import java.io.File
import java.security.MessageDigest
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger

import FileOps._

import scala.collection.mutable

class Hasher {

    val logger: Logger = Logger.getLogger(this.getClass.getName)
    // 32 byte long
    val MD5: String = "MD5"
    // 64 byte long
    val SHA256: String = "SHA-256"
    // temporary pitstop file that gets created when you do 'delorean add <files>'
    val pitstopsFolder: File = new File(".tm/pitstops/")

    def computeHashOfAddedFiles(filePaths: Array[String]): Unit = {
        var fileNameFileHashMap: mutable.LinkedHashMap[String, String] = mutable.LinkedHashMap.empty
        filePaths.foreach { file ⇒
            fileNameFileHashMap += (computeHashOfFile(file) → file)
        }
        val files: Array[File] = filesMatchingInDir(pitstopsFolder, fileName ⇒ fileName.startsWith("_temp"))
        val tempPitstopFile = if (files.nonEmpty) files(0) else File.createTempFile("_temp", null, pitstopsFolder)
        // write the hashes of all added files to temp pitstop file
        writeMapToFile(fileNameFileHashMap, null, tempPitstopFile)

        // Have to add more info about the commit like Pitstop time, Rider name and Rider log
    }

    // Creates metadata file with same name as that of the pitstop hash
    def createMetadataFile(pitstopHash: String, riderLog: String): Unit = {
        val time = s"Time:${ZonedDateTime.now.format(DateTimeFormatter.ofPattern("MMM dd yyyy hh:mm a zzzz"))}\n"
        val rider = if (Configuration("rider").nonEmpty) Configuration("rider") else System.getProperty("user.name")
        val timeAndRider = time + s"Rider:$rider\n"
        // parent pitstop would be whatever is present in the current indicator file.
        val parentPitstop = getLinesOfFile(".tm/indicators/current").head
        val timeAndRiderAndParents = timeAndRider + s"Parent(s):\n$parentPitstop\n"
        val fullMetadata = timeAndRiderAndParents + s"RiderLog:\n$riderLog"

        writeToFile(s".tm/metadata/$pitstopHash", fullMetadata)
    }

    // Hash for a List of files
    def computePitStopHash(riderLog: String): Unit = {
        // Generate pitstop hash as the temp file
        val files: Array[File] = filesMatchingInDir(pitstopsFolder, fileName ⇒ fileName.startsWith("_temp"))
        if (files.isEmpty) {
            println("Delorean is all charged up. No need for Pitstops.")
            return
        }
        val tempPitstopFile = files(0)
        tempPitstopFile.deleteOnExit()
        val tempPitstopFilePath = tempPitstopFile.getPath
        val pitstopHash: String = computeHashOfFile(tempPitstopFilePath)

        // Copy temp file's to that of the pitstop hash
        copyFile(tempPitstopFilePath, s".tm/pitstops/$pitstopHash")
        createMetadataFile(pitstopHash, riderLog)
        writeToFile(".tm/indicators/current", pitstopHash)

        // Once the temp file is copied, we can delete it
        //        Files.delete(Paths.get(tempPitstopFile))
        //        println(f.getAbsolutePath + ", " + f.canRead + ", " + f.canWrite)
        //        if (f.delete) println("Deleted") else println("Not deleted")
    }

    def computeHash(str: String, hash: String): String = {
        MessageDigest.getInstance(hash).digest(str.getBytes).map("%02x".format(_)).mkString
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
        addHashesAndContentOfLinesToPool(hashLineMap, ".tm/string_pool")

        // Compute SHA-256 Hash of all lines of a file combined to get the file hash
        val fileHash: String = computeHash(lines.mkString("\n"), SHA256)

        // Add line hashes to hashes file
        addLineHashesToHashesFile(hashLineMap.keys, s".tm/hashes/$fileHash")

        // Once the file hash is computed, Add it to travelogue file
        addToTravelogueFile((filePath, fileHash))

        // return filHash
        fileHash
    }
}