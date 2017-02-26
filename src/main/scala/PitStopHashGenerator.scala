import java.io.{FileWriter, PrintWriter}
import java.nio.file.{FileAlreadyExistsException, Files, Paths}
import java.security.MessageDigest

import scala.io.Source

class PitStopHashGenerator {

    // 40 byte long
    val SHA1: String = "SHA1"
    // 64 byte long
    val SHA256: String = "SHA-256"

    // Hash for a List of files
    def computePitStopHash(filePaths: Array[String]): String = {
        val concatenatedFileAddresses: StringBuilder = new StringBuilder
        filePaths.foreach(x ⇒ concatenatedFileAddresses.append(computeHashOfAFile(x)))
        println(concatenatedFileAddresses.toString)
        computeHash(concatenatedFileAddresses.toString, SHA256)
    }

    def computeHashOfAFile(filePath: String): Unit = {
        println(s"Caculating hash for file: $filePath")
        var hashLineMap: Map[String, String] = Map.empty

        // Get all lines of the file as an Iterator
        val lines: Iterator[String] = getLinesOfFile(filePath)
        println(s"lines = ${lines.mkString("\n")}")

        // Compute SHA-1 Hash of each line and create a Map of (line_hash -> line)
        lines.foreach { x => hashLineMap = hashLineMap + (computeHash(x, SHA1) → x) }

        println(s"Generated hashmap: $hashLineMap")

        // Add line_hash - line to the string pool file
        addHashesAndContentOfLinesToPool(hashLineMap, ".tm/string_pool")

        // Compute SHA-256 Hash of all lines of a file combined to get the file hash
        val fileHash: String = computeHash(lines.mkString("\n"), SHA256)
        println(s"hash generated: $fileHash")

        // Add line hashes to hashes file
        addLineHashesToHashesFile(hashLineMap.keys, s".tm/hashes/$fileHash")

        // Once the file hash is computed, Add it to travelogue file
        addToTravelogueFile(fileHash, filePath)

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

    def addToTravelogueFile(fileHash: String, filePath: String): Unit = {
        val travelogueFile = ".tm/travelogue"
        var map: Map[String, String] = getFileAsMap(travelogueFile)

        // If the exact filePath -> fileHash exists in the map, Do nothing. But if not, it means the file has changed.
        // So we add in the current key value pair which just updates the value of existing key
        if (!map.exists(_ == (filePath → fileHash))) {
            map += (filePath → fileHash)
        }

        // Once the map is populated, write the map to travelogue file
        writeMapToFile(map, travelogueFile)
    }

    def writeMapToFile(map: Map[String, String], filePath: String): Unit = {
        // printwriter empties the contents of a file if it exists
        val writer: PrintWriter = new PrintWriter(filePath)
        map.foreach(tuple ⇒ writer.write(s"${tuple._1}: ${tuple._2}\n"))
        writer.flush()
    }

    // returns the "filename: fileHash" file as a Map of (filename -> fileHash
    def getFileAsMap(filePath: String): Map[String, String] = {
        var filenameHashMap: Map[String, String] = Map.empty
        if (!createIfDoesNotExist(filePath)) {
            getLinesOfFile(filePath).foreach(line ⇒ {
                val splits = line.split(":")
                filenameHashMap += (splits(0) → splits(1))
            })
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


    def appendStringToFile(fileName: String, content: String): Unit = {
        createIfDoesNotExist(fileName)
        val writer: FileWriter = new FileWriter(fileName, true)
        writer.write(content)
        writer.close()
    }

}