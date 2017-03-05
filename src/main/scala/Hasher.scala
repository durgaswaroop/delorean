import java.io.File
import java.security.MessageDigest
import java.util.logging.Logger

import FileOps._

class Hasher {

    val logger: Logger = Logger.getLogger(this.getClass.getName)
    // 32 byte long
    val MD5: String = "MD5"
    // 64 byte long
    val SHA256: String = "SHA-256"

    // Hash for a List of files
    def computePitStopHash(filePaths: Array[String]): Unit = {
        if (filePaths.length == 0) {
            println("Not enough arguments")
            System.exit(1)
        }
        var fileNamefileHashMap: Map[String, String] = Map.empty
        filePaths.foreach { file ⇒
            fileNamefileHashMap += (computeHashOfFile(file) → file)
        }
        // write the hashes of all files to temp file
        val tempPitstopFile = ".tm/pitstops/_temp"
        writeMapToFile(fileNamefileHashMap, tempPitstopFile)

        // Generate pitstop hash as the temp file
        val pitstopHash: String = computeHashOfFile(tempPitstopFile)

        // Copy temp file's to that of the pitstop hash
        copyFile(tempPitstopFile, s".tm/pitstops/$pitstopHash")

    }

    def computeHash(str: String, hash: String): String = {
        MessageDigest.getInstance(hash).digest(str.getBytes).map("%02x".format(_)).mkString("")
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