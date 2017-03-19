package delorean

import java.io.{File, FileWriter, FilenameFilter, PrintWriter}
import java.nio.file._
import java.util.function.Predicate
import java.util.stream.Collectors

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.io.Source

/**
  * Created by dperla on 13-03-2017.
  */
object FileOps {

    def filesMatchingInDir(dir: File, check: String ⇒ Boolean): Array[File] = {
        dir.listFiles(new FilenameFilter {
            override def accept(dir: File, name: String): Boolean = check(name)
        })
    }

    def getFilesRecursively(dir: String, condition: Predicate[Path] = _ ⇒ true): List[String] = {
        val files: List[Path] = Files.walk(Paths.get(dir)).filter(condition).collect(Collectors.toList()).asScala.toList
        files.map(p ⇒ p.normalize.toString)
    }

    /**
      * Writes content to file. Overwrites the existing content.
      *
      * @param filePath : path to the file
      * @param content  : Content to write to the file
      */
    def writeToFile(filePath: String, content: String): Unit = {
        createIfDoesNotExist(filePath)
        scala.tools.nsc.io.File(filePath).writeAll(content)
    }

    def getLinesOfFile(filePath: String): List[String] = {
        val source = Source.fromFile(filePath, "UTF-8")
        val lines = source.getLines().toList
        source.close()
        lines
    }

    // Reads the current file as a map. Adds new things to add to this map and writes this entire map to file
    // overwriting the existing content.
    def addHashesAndContentOfLinesToPool(hashLineMap: mutable.LinkedHashMap[String, String], stringPoolFile: String): Unit = {
        var fileMap: mutable.LinkedHashMap[String, String] = getFileAsMap(stringPoolFile)

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
    def getFileAsMap(filePath: String): mutable.LinkedHashMap[String, String] = {
        var filenameHashMap: mutable.LinkedHashMap[String, String] = mutable.LinkedHashMap.empty
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

    // Overwrites the existing content.
    def writeMapToFile(map: mutable.LinkedHashMap[String, String], filePath: String, fileToAppendTo: File = null): Unit = {
        // printwriter empties the contents of a file if it exists
        val writer: PrintWriter = {
            if (fileToAppendTo == null) new PrintWriter(filePath)
            else new PrintWriter(new FileWriter(fileToAppendTo, true))
        }
        map.foreach(tuple ⇒ writer.write(s"${tuple._1}:${tuple._2}\n"))
        writer.flush()
        writer.close()
    }

    // Overwrites the existing content.
    def addLineHashesToHashesFile(lineHashes: List[String], file: String): Unit = {
        // printwriter empties the contents of a file if it exists
        val writer: PrintWriter = new PrintWriter(file)
        lineHashes.foreach(x ⇒ writer.write(s"$x\n"))
        writer.flush()
        writer.close()
    }

    // Reads the current file as a map. Adds new things to add to this map and writes this entire map to file
    // overwriting the existing content.
    def addToTravelogueFile(hashNameTuple: (String, String)): Unit = {
        var map: mutable.LinkedHashMap[String, String] = getFileAsMap(TRAVELOGUE)

        // If the exact filePath -> fileHash exists in the map, Do nothing. But if not, it means the file has changed.
        // So we add in the current key value pair which just updates the value of existing key
        if (!map.exists(_ == (hashNameTuple._1 → hashNameTuple._2))) {
            map += (hashNameTuple._1 → hashNameTuple._2)
        }

        // Once the map is populated, write the map to travelogue file
        writeMapToFile(map, TRAVELOGUE)
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

    def getCurrentPitstop: String = {
        val currentPitstopOrTimeline: String = getLinesOfFile(CURRENT_INDICATOR).head
        if (Files.exists(Paths.get(INDICATORS_FOLDER + currentPitstopOrTimeline))) {
            val lines: List[String] = getLinesOfFile(INDICATORS_FOLDER + currentPitstopOrTimeline)
            if (lines.nonEmpty) lines.head else ""
        } else {
            currentPitstopOrTimeline
        }
    }

    // Gets the parent pitstop of the given pitstop. Would be an empty string if no parent is present.
    def parent(pitstop: String): String = {
        val parent: String = getLinesOfFile(METADATA_FOLDER + pitstop).filter(_.contains("Parent")).head.split(":", 2)(1)
        parent
    }

    def getFilesInThePitstop(pitstop: String): List[String] = {
        val filesAndHashMap = getFileAsMap(PITSTOPS_FOLDER + pitstop)
        filesAndHashMap.values.toList
    }

    def getTempPitstopFile: String = {
        val tempFileArray: Array[File] = filesMatchingInDir(new File(PITSTOPS_FOLDER), fileName ⇒ fileName.startsWith("_temp"))
        if (tempFileArray.nonEmpty) tempFileArray(0).getPath else ""
    }

    // hash -> fileName
    def getHashesOfAllFilesKnownToDelorean: Map[String, String] = {
        var currentPitstop = getCurrentPitstop
        var map: Map[String, String] = Map.empty
        while (currentPitstop.nonEmpty) {
            val pitstopMap = getFileAsMap(PITSTOPS_FOLDER + currentPitstop)
            pitstopMap.foreach(kvPair ⇒ if (!map.contains(kvPair._1)) map = map + kvPair)
            currentPitstop = parent(currentPitstop)
        }

        // Apart from looking at the pitstops, also looks at the files in _temp file.
        // Those files shouldn't come in the untracked files too.
        val tempPitstopFile = getTempPitstopFile
        if (tempPitstopFile.nonEmpty) {
            val tempFilePitstopMap = getFileAsMap(getTempPitstopFile)
            tempFilePitstopMap.foreach(kvPair ⇒ if (!map.contains(kvPair._1)) map = map + kvPair)
        }
        map
    }
}
