package delorean

import java.io.File
import java.nio.file.{Files, Paths}
import java.security.MessageDigest
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger

import delorean.FileOps._

import scala.collection.mutable
import scala.util.Try

class Hasher {

    val logger: Logger = Logger.getLogger(this.getClass.getName)
    // 32 byte long
    val MD5: String = "MD5"
    // 64 byte long
    val SHA256: String = "SHA-256"
    // temporary pitstop file that gets created when you do 'delorean add <files>'
    val PITSTOPS_FOLDER_FILE: File = new File(PITSTOPS_FOLDER)

    def computeHashOfAddedFiles(files: List[String]): Unit = {
        var fileNameFileHashMap: mutable.LinkedHashMap[String, String] = mutable.LinkedHashMap.empty

        // Compute hash of each added file
        files foreach { file ⇒ fileNameFileHashMap += (computeFileHash(file) → file) }

        // Once the hashes are computed, check for the presence of a "_temp" file.
        // Existence of "_temp" file means that there were a few more files added before and not committed.
        val tempFiles: Array[File] = filesMatchingInDir(PITSTOPS_FOLDER_FILE, fileName ⇒ fileName.startsWith("_temp"))

        // So, if the file exists, add information about the newly added files to that or else create a new temp file.
        val tempPitstopFile = if (tempFiles.nonEmpty) tempFiles(0) else File.createTempFile("_temp", null, PITSTOPS_FOLDER_FILE)
        // write the hashes of all added files to temp pitstop file
        writeMapToFile(fileNameFileHashMap, null, tempPitstopFile)
    }

    /**
      * Computes the hash of the given file and also does the relavant file operations such as storing the hashes to file,
      * writing line contents to string pool etc.
      * But if 'justGetTheHash' is true, we'll just calculate the file hash without writing anything to files.
      *
      * @param filePath       : Path of the file
      * @param justGetTheHash : Just want to get the hash without doing the full computing process.
      * @return
      */
    def computeFileHash(filePath: String, justGetTheHash: Boolean = false): String = {
        var hashLineMap: mutable.LinkedHashMap[String, String] = mutable.LinkedHashMap.empty

        // Get all lines of the file as a List
        val lines = getLinesOfFile(filePath)
        logger.fine(s"Lines:\n$lines\n")

        // Compute SHA-1 Hash of each line and create a Map of (line_hash -> line)
        lines.foreach(x => hashLineMap += (computeStringHash(x, MD5) → x))
        logger.fine(s"Map:\n$hashLineMap\n")

        // Compute SHA-256 Hash of all lines of a file combined to get the file hash
        val fileHash: String = computeStringHash(lines.mkString("\n"), SHA256)

        if (!justGetTheHash) {
            // Add line_hash - line to the string pool file
            addHashesAndContentOfLinesToPool(hashLineMap, STRING_POOL)

            // Add line hashes to hashes file
            addLineHashesToHashesFile(hashLineMap.keys.toList, HASHES_FOLDER + fileHash)

            // Once the file hash is computed, Add it to travelogue file
            addToTravelogueFile((filePath, fileHash))
        }

        // return filHash
        fileHash
    }

    // Hash for a List of files
    def computePitStopHash(riderLog: String): Unit = {
        // Generate pitstop hash as the temp file
        val files: Array[File] = filesMatchingInDir(PITSTOPS_FOLDER_FILE, fileName ⇒ fileName.startsWith("_temp"))

        // When temp file is not present nothing to do because no files are 'added' yet
        if (files.isEmpty) {
            println("No files added. Delorean is all charged up. No need for Pitstops.")
            return
        }

        val tempPitstopFile = files(0)
        tempPitstopFile.deleteOnExit()
        val tempPitstopFilePath = tempPitstopFile.getPath
        val allFilesHash: String = computeFileHash(tempPitstopFile.getPath)
        val metadataAndHash: (String, String) = computeMetadataAndItsHash(riderLog)
        val metadata = metadataAndHash._1
        val metadataHash = metadataAndHash._2

        // Pitstop hash will be computed as the hash for the combined string of allFilesHash and metadataHash
        val pitstopHash = computeStringHash(allFilesHash + metadataHash, SHA256)

        // Copy temp file's to that of the pitstop hash
        copyFile(tempPitstopFilePath, PITSTOPS_FOLDER + pitstopHash)
        writeToFile(METADATA_FOLDER + pitstopHash, metadata)

        // Write the new pitstop hash into the current indicator
        val currentTimeLine = getLinesOfFile(CURRENT_INDICATOR).head
        // If the current file is pointing to an actual timeline
        if (currentTimeLine.nonEmpty) writeToFile(INDICATORS_FOLDER + currentTimeLine, pitstopHash)

        // Once the temp file is copied, we can delete it
        Try(Files.delete(Paths.get(tempPitstopFilePath)))
    }

    def computeMetadataAndItsHash(riderLog: String): (String, String) = {
        val time = s"Time:${ZonedDateTime.now.format(DateTimeFormatter.ofPattern("MMM dd yyyy hh:mm a zzzz"))}\n"
        val rider = if (Configuration("rider").nonEmpty) Configuration("rider") else System.getProperty("user.name")
        val timeAndRider = time + s"Rider:$rider\n"

        // parent pitstop would be whatever is present in the current indicator file which would become the parent once
        // the new pitstop is calculated
        val currentTimeLine: String = getLinesOfFile(CURRENT_INDICATOR).head
        var lines = List[String]("")
        if (Files.exists(Paths.get(INDICATORS_FOLDER + currentTimeLine))) lines = getLinesOfFile(INDICATORS_FOLDER + currentTimeLine)

        // The current file may be empty for the first commit. In that case we will just put an empty string
        val parentPitstop = if (lines.nonEmpty) lines.head else ""

        val timeAndRiderAndParents = timeAndRider + s"Parent(s):$parentPitstop\n"
        val fullMetadata = timeAndRiderAndParents + s"RiderLog:\n$riderLog"

        // return the hash of the fullMetadata string along with the fullMetadata string itself
        (fullMetadata, computeStringHash(fullMetadata, SHA256))
    }

    def computeStringHash(str: String, hash: String): String = {
        MessageDigest.getInstance(hash).digest(str.getBytes).map("%02x".format(_)).mkString
    }
}