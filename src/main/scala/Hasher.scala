import java.io.{File, PrintWriter}
import java.nio.file.{FileAlreadyExistsException, Files, Paths, StandardCopyOption}
import java.security.MessageDigest

import com.typesafe.scalalogging.slf4j.LazyLogging

import scala.io.Source

class Hasher extends LazyLogging {

    // 32 byte long
    val MD5: String = "MD5"
    // 64 byte long
    val SHA256: String = "SHA-256"

    // Hash for a List of files
    def computePitStopHash(filePaths: Array[String]): Unit = {
        var fileNamefileHashMap: Map[String, String] = Map.empty
        filePaths.foreach { file ⇒
            fileNamefileHashMap += (computeHashOfFile(file) → file)
        }
        // write the hashes of all files to temp file
        val tempPitstopFile = ".tm/pitstops/_temp"
        writeMapToFile(fileNamefileHashMap, tempPitstopFile)

        // Generate pitstop hash as the temp file
        val pitstopHash: String = computeHashOfFile(tempPitstopFile)

        // Rename temp file's name to that of the pitstop hash
        Files.copy(Paths.get(tempPitstopFile), Paths.get(s".tm/pitstops/$pitstopHash"), StandardCopyOption.REPLACE_EXISTING)
//        Files.move(Paths.get(tempPitstopFile), Paths.get(s".tm/pitstops/$pitstopHash"), StandardCopyOption.REPLACE_EXISTING)
        new File(tempPitstopFile).delete()
    }

    def computeHashOfFile(filePath: String): String = {
        var hashLineMap: Map[String, String] = Map.empty

        // Get all lines of the file as a List
        val lines = getLinesOfFile(filePath)
        logger.debug(s"Lines:\n$lines\n")

        // Compute SHA-1 Hash of each line and create a Map of (line_hash -> line)
        lines.foreach(x => hashLineMap += (computeHash(x, MD5) → x))
        logger.debug(s"Map:\n$hashLineMap\n")

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

    def getLinesOfFile(filePath: String): List[String] = Source.fromFile(filePath).getLines().toList

    def computeHash(str: String, hash: String): String = {
        MessageDigest.getInstance(hash).digest(str.getBytes).map("%02x".format(_)).mkString("")
    }

    def addHashesAndContentOfLinesToPool(hashLineMap: Map[String, String], stringPoolFile: String): Unit = {
        var fileMap: Map[String, String] = getFileAsMap(stringPoolFile)

        hashLineMap.foreach(tuple ⇒ {
            if (!fileMap.contains(tuple._1)) {
                fileMap += tuple
            }
        })

        // Once the map is populated, write the map to travelogue file
        writeMapToFile(fileMap, stringPoolFile)
    }

    // returns the "filename: fileHash" file as a Map of (filename -> fileHash
    // OR
    // returns the lineHash: lineContent Map
    def getFileAsMap(filePath: String): Map[String, String] = {
        var filenameHashMap: Map[String, String] = Map.empty
        if (!createIfDoesNotExist(filePath)) {
            val fileLines = getLinesOfFile(filePath)
            if (fileLines.nonEmpty) {
                fileLines.foreach(line ⇒ {
                    // split(str, int) required to make sure that the splits array we get should have only two elements
                    // at the max. Other wise, it splits at every instance of ":" and we will get a lot more things in
                    // the splits array
                    val splits = line.split(":", 2)
                    filenameHashMap += (splits(0) → splits(1))
                })
            }
        }
        filenameHashMap
    }

    def writeMapToFile(map: Map[String, String], filePath: String): Unit = {
        // printwriter empties the contents of a file if it exists
        val writer: PrintWriter = new PrintWriter(filePath)
        map.foreach(tuple ⇒ writer.write(s"${tuple._1}:${tuple._2}\n"))
        writer.flush()
    }

    def addLineHashesToHashesFile(lineHashes: Iterable[String], file: String): Unit = {
        // printwriter empties the contents of a file if it exists
        val writer: PrintWriter = new PrintWriter(file)
        lineHashes.foreach(x ⇒ writer.write(s"$x\n"))
        writer.flush()
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