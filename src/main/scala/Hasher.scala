import java.io.{File, FileWriter, PrintWriter}
import java.nio.file.{FileAlreadyExistsException, Files, Paths}
import java.security.MessageDigest

import scala.io.Source

class Hasher {

    // 32 byte long
    val MD5: String = "MD5"
    // 64 byte long
    val SHA256: String = "SHA-256"

    // Hash for a List of files
    //    def computePitStopHash(filePaths: Array[String]): String = {
    //        val concatenatedFileAddresses: StringBuilder = new StringBuilder
    //        filePaths.foreach(x ⇒ concatenatedFileAddresses.append(computeHashOfAFile(x)))
    //        println(concatenatedFileAddresses.toString)
    //        computeHash(concatenatedFileAddresses.toString, SHA256)
    //    }

    def computeHashOfFile(filePath: String): Unit = {
        var hashLineMap: Map[String, String] = Map.empty

        // Get all lines of the file as a List
        val lines: List[String] = getLinesOfFile(filePath).toList

        // Compute SHA-1 Hash of each line and create a Map of (line_hash -> line)
        lines.foreach(x => hashLineMap += (computeHash(x, MD5) → x))

        // Add line_hash - line to the string pool file
        addHashesAndContentOfLinesToPool(hashLineMap, ".tm/string_pool")

        // Compute SHA-256 Hash of all lines of a file combined to get the file hash
        val fileHash: String = computeHash(lines.mkString("\n"), SHA256)

        // Add line hashes to hashes file
        addLineHashesToHashesFile(hashLineMap.keys, s".tm/hashes/$fileHash")

        // Once the file hash is computed, Add it to travelogue file
        addToTravelogueFile((filePath, fileHash))
    }

    def addLineHashesToHashesFile(lineHashes: Iterable[String], file: String): Unit = {
        // printwriter empties the contents of a file if it exists
        val writer: PrintWriter = new PrintWriter(file)
        lineHashes.foreach(x ⇒ writer.write(s"$x\n"))
        writer.flush()
    }

    def getLinesOfFile(filePath: String): Iterator[String] = Source.fromFile(filePath).getLines()

    def computeHash(str: String, hash: String): String = {
        MessageDigest.getInstance(hash).digest(str.getBytes).map("%02x".format(_)).mkString("")
    }

    def addHashesAndContentOfLinesToPool(hashLineMap: Map[String, String], hashFilePath: String): Unit = {
        var fileMap: Map[String, String] = getFileAsMap(hashFilePath)

        hashLineMap.foreach(tuple ⇒ {
            if (!fileMap.contains(tuple._1)) {
                fileMap += tuple
            }
        })

        // Once the map is populated, write the map to travelogue file
        writeMapToFile(fileMap, hashFilePath)
    }

    def addToTravelogueFile(hashNameTuple: (String, String)): Unit = {
        val travelogueFile = ".tm/travelogue"
        var map: Map[String, String] = getFileAsMap(travelogueFile)

        // If the exact filePath -> fileHash exists in the map, Do nothing. But if not, it means the file has changed.
        // So we add in the current key value pair which just updates the value of existing key
        if (!map.exists(_ == (hashNameTuple._1 → hashNameTuple._2))) {
            map += (hashNameTuple._1 → hashNameTuple._2)
        }

        // Once the map is populated, write the map to travelogue file
        writeMapToFile(map, travelogueFile)
    }

    def writeMapToFile(map: Map[String, String], filePath: String): Unit = {
        // printwriter empties the contents of a file if it exists
        val writer: PrintWriter = new PrintWriter(filePath)
        map.foreach(tuple ⇒ writer.write(s"${tuple._1}:${tuple._2}\n"))
        writer.flush()
    }

    // returns the "filename: fileHash" file as a Map of (filename -> fileHash
    def getFileAsMap(filePath: String): Map[String, String] = {
        var filenameHashMap: Map[String, String] = Map.empty
        if (!createIfDoesNotExist(filePath)) {
            val fileLines = getLinesOfFile(filePath)
            if (fileLines.nonEmpty) {
                fileLines.foreach(line ⇒ {
                    val splits = line.split(":")
                    filenameHashMap += (splits(0) → splits(1))
                })
            }
        }
        filenameHashMap
    }

    // returns false if the file already exists
    def createIfDoesNotExist(filePath: String): Boolean = {
        try {
            Files.createFile(Paths.get(filePath))
            true
        } catch {
            case _: FileAlreadyExistsException ⇒ false
        }
    }

}