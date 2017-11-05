/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean

import java.io.File
import java.nio.file.{Files, Path, Paths}
import java.security.MessageDigest
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger

import com.freblogg.hashing.FNV
import delorean.FileOps._

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

object Hasher {

  val logger: Logger = Logger.getLogger(this.getClass.getName)

  // 16 byte long
  val FNV1A64: String = "FNV1a64"

  // 64 byte long
  val SHA256: String = "SHA-256"

  val PITSTOPS_FOLDER_FILE: File = new File(PITSTOPS_FOLDER)

  /**
    * Do the hashing process for all the staged files.
    *
    * Hashes of each of the staged files is computed and added into fileNameFileHashMap map.
    * Then add them into the temp file (create it if it doesn't exist) from the Map
    *
    * @param files : Staged files
    */
  def computeHashOfStagedFiles(files: List[String]): Unit = {
    var fileNameFileHashMap: mutable.LinkedHashMap[String, String] =
      mutable.LinkedHashMap.empty

    /*
                    This is for cases when you don't want to continue the hashing process for a file,
                    But still want to add it to the temp file
     */
    var nameHashMapToAddToTempFile: mutable.LinkedHashMap[String, String] =
      mutable.LinkedHashMap.empty

    val allFilesAndHashesKnownToDelorean: Map[Path, String] =
      getHashesOfAllFilesKnownToDelorean

    /*
                    Compute hash of each staged file. But add it to fileNameFileHashMap only if the exact (hash, file) pair
                    is not present in the allFilesAndHashesKnownToDelorean map.
                    This way it will be added to temp file only if the file has actually changed.
     */
    files foreach (file => {
      val hash: String = FileDictionary(file, hashNeeded = true).hash
      logger.fine(s"Hash computed: $hash")

      // If the exact  file -> hash pair exists, we don't have to do anything for that file anymore
      if (!allFilesAndHashesKnownToDelorean.exists(
            _ == (Paths.get(file) -> hash))) {

        /*
                                    If the hash file already exists, but the current hash is not known to Delorean (happens when the file was previously
                                    staged and then unstaged afterwards. In that case we don't want to continue the hashing process again but
                                    still want the file to be added to the _temp file as it was asked for.
         */
        if (Files.exists(Paths.get(HASHES_FOLDER + hash)))
          nameHashMapToAddToTempFile += (file -> hash)

        // This should be done regardless of above condition
        fileNameFileHashMap += (file -> hash)
      }
    })
    logger.fine(s"(+++)FileNameFileHashMap : $fileNameFileHashMap")

    // If the map is empty,there is nothing else to be done
    if (fileNameFileHashMap.isEmpty) return

    fileNameFileHashMap.keySet.foreach(continueFullHashingProcess)

    /*
                   Once the hashes are computed, check for the presence of a "_temp" file.
                   Existence of "_temp" file means that there were a few more files 'staged' before but not committed.
                   So, if the file exists, add information about the newly staged files to that or else create a new temp file.
     */
    val tempPitstopFile = {
      if (getTempPitstopFileLocation.nonEmpty)
        new File(getTempPitstopFileLocation)
      else File.createTempFile("_temp", null, PITSTOPS_FOLDER_FILE)
    }

    // write the hashes of all staged files to temp pitstop file
    var existingTempFileMap: mutable.LinkedHashMap[String, String] =
      getFileAsMap(tempPitstopFile.getPath)

    // Update the existing tempFileMap with the newly staged files and then write it back
    fileNameFileHashMap.foreach(existingTempFileMap += _)
    nameHashMapToAddToTempFile.foreach(existingTempFileMap += _)
    writeMapToFile(existingTempFileMap, tempPitstopFile.getPath)
  }

  /**
    * Computes the hash of the given file and also does the relevant file operations such as storing the hashes to file,
    * writing line contents to string pool etc.
    *
    * @param filePath : Path of the file
    */
  def continueFullHashingProcess(filePath: String): Unit = {
    logger.fine(s"called with params: $filePath")
    var hashLineMap: mutable.LinkedHashMap[String, String] =
      mutable.LinkedHashMap.empty

    // Get all lines of the file as a List
    val lines = FileDictionary(filePath, linesNeeded = true).lines

    // Compute SHA-256 Hash of all lines of a file combined to get the file hash
    val fileHash: String = FileDictionary(filePath, hashNeeded = true).hash

    /*
                  When its a binary file, don't do all the usual line extractions and hashing.
                  Just put the file into BINARIES_FOLDER with the filehash as the name
     */
    if (isBinaryFile(filePath) && Files.notExists(
          Paths.get(BINARIES_FOLDER + fileHash))) {
      copyFile(filePath, BINARIES_FOLDER + fileHash)

      // Once the file hash is computed, Add it to travelogue file
      addToTravelogueFile((filePath, fileHash))
      return
    }
    logger.finest(s"Lines:\n$lines\n")

    // Compute SHA-1 Hash of each line and create a Map of (line_hash -> line)
    lines.foreach(x => hashLineMap += (computeStringHash(x, FNV1A64) -> x))
    logger.finest(s"Map:\n$hashLineMap\n")

    // Add line_hash - line to the string pool file
    addHashesAndContentOfLinesToPool(hashLineMap, STRING_POOL)

    // Add line hashes to hashes file
    addLineHashesToHashesFile(hashLineMap.keys.toList, HASHES_FOLDER + fileHash)

    // Once the file hash is computed, Add it to travelogue file
    addToTravelogueFile((filePath, fileHash))
  }

  def computeStringHash(str: String, hash: String): String = {
    if (hash == FNV1A64) FNV.fnv1a64(str)
    else
      MessageDigest
        .getInstance(hash)
        .digest(str.getBytes)
        .map("%02x".format(_))
        .mkString
  }

  /**
    * Compute pitstop hash for the currently staged files and do the pitstopping procedure for those files.
    *
    * First we compute the required metadata and the metadataHash by calling the computeMetadataAndItsHash() method.
    * Then we compute the pitstop hash as the string hash of the combined string of hash of all staged files and metadata hash.
    *
    * @param riderLog : Rider log given for the pitstop
    */
  def computePitStopHash(riderLog: String): Unit = {
    val tempPitstopFile = getTempPitstopFileLocation

    // When temp file is not present nothing to do because no files are 'staged' yet
    if (tempPitstopFile isEmpty) {
      println(
        "No files staged. Delorean is all charged up. No need for Pitstops.")
      return
    }

    val (metadata, metadataHash): (String, String) = computeMetadataAndItsHash(
      riderLog)

    // We just need the hash here. Not the other parts after that.
    val allFilesHash: String =
      FileDictionary(tempPitstopFile, hashNeeded = true).hash

    // Pitstop hash will be computed as the hash for the combined string of allFilesHash and metadataHash
    val pitstopHash = computeStringHash(allFilesHash + metadataHash, SHA256)

    // Copy temp file's to a file with the name of pitstop hash in the PITSTOPS_FOLDER
    copyFile(tempPitstopFile, PITSTOPS_FOLDER + pitstopHash)

    // Write metadata to the metadata file
    writeToFile(METADATA_FOLDER + pitstopHash, metadata)

    // Write the new pitstop hash into the current indicator
    val currentTimeLine = getLinesOfFile(CURRENT_INDICATOR).head

    // If the current file is pointing to an actual timeline
    if (currentTimeLine nonEmpty)
      writeToFile(INDICATORS_FOLDER + currentTimeLine, pitstopHash)

    /*
                    Once the temp file is copied, we can delete it.
                    But because of some stream unclosed issue, it won't get deleted.
                    So, we set the last modified time of the temp File and the pitstophash file such that
                    pitstopfile's time is greater than that of the temp file.
                    Using this we will delete it the next time a delorean command is called.
     */
    Try(Files.delete(Paths.get(tempPitstopFile))) match {
      case Failure(_) =>
        new File(tempPitstopFile).setLastModified(System.currentTimeMillis())
        new File(PITSTOPS_FOLDER + pitstopHash)
          .setLastModified(System.currentTimeMillis() + 10)
      case Success(_) =>
    }
  }

  def computeMetadataAndItsHash(riderLog: String): (String, String) = {
    val time =
      s"Time:${ZonedDateTime.now.format(DateTimeFormatter.ofPattern("MMM dd yyyy hh:mm a zzzz"))}\n"
    val rider =
      if (Configuration("rider").nonEmpty) Configuration("rider")
      else System.getProperty("user.name")
    val timeAndRider = time + s"Rider:$rider\n"

    /*
                    parent pitstop would be whatever is present in the current indicator file which would become the parent once
                    the new pitstop is calculated
     */
    val currentTimeLine: String = getLinesOfFile(CURRENT_INDICATOR).head
    var lines = List[String]("")
    if (Files.exists(Paths.get(INDICATORS_FOLDER + currentTimeLine)))
      lines = getLinesOfFile(INDICATORS_FOLDER + currentTimeLine)

    // The current file may be empty for the first commit. In that case we will just put an empty string
    val parentPitstop = if (lines.nonEmpty) lines.head else ""

    val timeAndRiderAndParents = timeAndRider + s"Parent(s):$parentPitstop\n"
    val fullMetadata = timeAndRiderAndParents + s"RiderLog:\n$riderLog"

    // return the hash of the fullMetadata string along with the fullMetadata string itself
    (fullMetadata, computeStringHash(fullMetadata, SHA256))
  }

  /**
    * Compute the hash of the file by reading its bytes and using that to directly get the SHA256 hash.
    *
    * @param fileName : File for which we need to calculate the hash
    * @return : Hash of the file if it exists or an empty string if it doesn't
    */
  def computeShaHash(fileName: String): String = {
    val filePath = Paths.get(fileName)
    if (!filePath.toFile.exists()) return ""
    val fileBytes = Files.readAllBytes(Paths.get(fileName))
    MessageDigest
      .getInstance(SHA256)
      .digest(fileBytes)
      .map("%02x".format(_))
      .mkString
  }
}
