import java.io.{File, FileWriter, FilenameFilter, PrintWriter}
import java.nio.file._

import scala.io.Source

object FileOps {

    def filesMatchingInDir(dir: File, check: String ⇒ Boolean): Array[File] = {
        dir.listFiles(new FilenameFilter {
            override def accept(dir: File, name: String) = check(name)
        })
    }

    def getLinesOfFile(filePath: String): List[String] = Source.fromFile(filePath).getLines().toList

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

    def writeMapToFile(map: Map[String, String], filePath: String, fileToAppendTo: File = null): Unit = {
        // printwriter empties the contents of a file if it exists
        val writer: PrintWriter = {
            if (fileToAppendTo == null) new PrintWriter(filePath)
            else new PrintWriter(new FileWriter(fileToAppendTo, true))
        }
        map.foreach(tuple ⇒ writer.write(s"${tuple._1}:${tuple._2}\n"))
        writer.flush()
        writer.close()
    }

    def addLineHashesToHashesFile(lineHashes: Iterable[String], file: String): Unit = {
        // printwriter empties the contents of a file if it exists
        val writer: PrintWriter = new PrintWriter(file)
        lineHashes.foreach(x ⇒ writer.write(s"$x\n"))
        writer.flush()
        writer.close()
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

    // copies file from src to dest
    def copyFile(src: String, dest: String): Path = Files.copy(Paths.get(src), Paths.get(dest),
        StandardCopyOption.REPLACE_EXISTING)


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
