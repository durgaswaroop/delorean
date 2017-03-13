package delorean

import java.io.File
import java.nio.file.attribute.FileTime
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

    def computeHashOfAddedFiles(filePaths: Array[String]): Unit = {
        var fileNameFileHashMap: mutable.LinkedHashMap[String, String] = mutable.LinkedHashMap.empty
        filePaths.foreach { file ⇒
            fileNameFileHashMap += (computeHashOfFile(file) → file)
        }
        val files: Array[File] = filesMatchingInDir(PITSTOPS_FOLDER_FILE, fileName ⇒ fileName.startsWith("_temp"))
        val tempPitstopFile = if (files.nonEmpty) files(0) else File.createTempFile("_temp", null, PITSTOPS_FOLDER_FILE)
        // write the hashes of all added files to temp pitstop file
        writeMapToFile(fileNameFileHashMap, null, tempPitstopFile)

        // Have to add more info about the commit like Pitstop time, Rider name and Rider log
    }

    def computeHashOfFile(filePath: String): String = {
        var hashLineMap: Map[String, String] = Map.empty

        // Get all lines of the file as a List
        val lines = getLinesOfFile(filePath)
        logger.fine(s"Lines:\n$lines\n")

        // Compute SHA-1 Hash of each line and create a Map of (line_hash -> line)
        lines.foreach(x => hashLineMap += (computeHash(x, MD5) → x))
        logger.fine(s"Map:\n$hashLineMap\n")

        // Add line_hash - line to the string pool file
        addHashesAndContentOfLinesToPool(hashLineMap, STRING_POOL)

        // Compute SHA-256 Hash of all lines of a file combined to get the file hash
        val fileHash: String = computeHash(lines.mkString("\n"), SHA256)

        // Add line hashes to hashes file
        addLineHashesToHashesFile(hashLineMap.keys, HASHES_FOLDER + fileHash)

        // Once the file hash is computed, Add it to travelogue file
        addToTravelogueFile((filePath, fileHash))

        // return filHash
        fileHash
    }

    // Hash for a List of files
    def computePitStopHash(riderLog: String): Unit = {
        // Generate pitstop hash as the temp file
        val files: Array[File] = filesMatchingInDir(PITSTOPS_FOLDER_FILE, fileName ⇒ fileName.startsWith("_temp"))

        val currentPitstop: String = if (getLinesOfFile(CURRENT_INDICATOR).nonEmpty) getLinesOfFile(CURRENT_INDICATOR).head else ""
        //if (currentPitstop)
        val currentPitstopTime: FileTime = Files.getLastModifiedTime(Paths.get(PITSTOPS_FOLDER + currentPitstop))
        val tempFileTime: FileTime = Files.getLastModifiedTime(Paths.get(files.head.getPath))

        if (files.isEmpty) {
            println("Delorean is all charged up. No need for Pitstops.")
            return
        }

//        else if (true) {
//            // currentPitstopTime.compareTo(tempFileTime) > 0
//            println(currentPitstopTime)
//            println(tempFileTime)
//            Files.delete(Paths.get(files.head.getPath))
//            return
//        }
//        println("After")
        val tempPitstopFile = files(0)
        tempPitstopFile.deleteOnExit()
        val tempPitstopFilePath = tempPitstopFile.getPath
        val allFilesHash: String = computeHashOfFile(tempPitstopFile.getPath)
        val metadataAndHash: (String, String) = computeMetadataAndItsHash(riderLog)
        val metadata = metadataAndHash._1
        val metadataHash = metadataAndHash._2

        // Pitstop hash will be computed as the hash for the combined string of allFilesHash and metadataHash
        val pitstopHash = computeHash(allFilesHash + metadataHash, SHA256)

        // Copy temp file's to that of the pitstop hash
        copyFile(tempPitstopFilePath, PITSTOPS_FOLDER + pitstopHash)
        writeToFile(METADATA_FOLDER + pitstopHash, metadata)
        // createMetadataFile(pitstopHash, riderLog)

        // Write the new pitstop hash into the current indicator
        writeToFile(CURRENT_INDICATOR, pitstopHash)

        // Once the temp file is copied, we can delete it
        Try(Files.delete(Paths.get(tempPitstopFilePath)))
    }

    def computeMetadataAndItsHash(riderLog: String): (String, String) = {
        val time = s"Time:${ZonedDateTime.now.format(DateTimeFormatter.ofPattern("MMM dd yyyy hh:mm a zzzz"))}\n"
        val rider = if (Configuration("rider").nonEmpty) Configuration("rider") else System.getProperty("user.name")
        val timeAndRider = time + s"Rider:$rider\n"

        // parent pitstop would be whatever is present in the current indicator file.
        val lines = getLinesOfFile(CURRENT_INDICATOR)
        // The current file may be empty for the first commit. In that case we will just put an empty string
        val parentPitstop = if (lines.nonEmpty) lines.head else ""

        val timeAndRiderAndParents = timeAndRider + s"Parent(s):$parentPitstop\n"
        val fullMetadata = timeAndRiderAndParents + s"RiderLog:\n$riderLog"

        // return the hash of the fullMetadata string along with the fullMetadata string itself
        (fullMetadata, computeHash(fullMetadata, SHA256))
    }

    def computeHash(str: String, hash: String): String = {
        MessageDigest.getInstance(hash).digest(str.getBytes).map("%02x".format(_)).mkString
    }
}