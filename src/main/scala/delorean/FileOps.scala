/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean

import java.io.{File, FileWriter, FilenameFilter, PrintWriter}
import java.nio.file._
import java.util.function.Predicate
import java.util.logging.Logger
import java.util.stream.Collectors

import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.{IOFileFilter, TrueFileFilter}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.io.{BufferedSource, Source}
import scala.util.Try

/**
  * Created by dperla on 13-03-2017.
  */
object FileOps {
  val logger: Logger = Logger.getLogger(this.getClass.getName)

  def getFilesRecursively(dir: String, condition: Predicate[Path] = _ => true): List[String] = {
    logger.fine(s"Getting files recursively in directory $dir.")

    val directory = new File(dir)
    val directoryAbsolutePath = directory.toPath.toAbsolutePath

    // Get all the files in the directory recursively but ignore the directory itself.
    // listFilesAndDirs returns the base directory also as part of the list. So, filter that out
    val fileList: List[String] = FileUtils
      .listFilesAndDirs(directory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
      .asScala
      .map(_.toPath.normalize().toAbsolutePath)
      .filterNot(path => path == directoryAbsolutePath)
      .map(_.toString)
      .toList

    logger.fine(s"List returned: $fileList")
    fileList
  }

  /**
    * Writes content to file. Overwrites the existing content.
    *
    * @param filePath : path to the file
    * @param content  : Content to write to the file
    */
  def writeToFile(filePath: String, content: String): Unit = {
    createIfDoesNotExist(filePath)
    logger.fine(s"Writing '$content' to file '$filePath'")
    scala.tools.nsc.io.File(filePath).writeAll(content)
  }

  // returns false if the file already exists
  def createIfDoesNotExist(filePath: String): Boolean = {
    try {
      Files.createFile(Paths.get(filePath))
      logger.finer(s"File $filePath does not exist before. Created it.")
      true
    } catch {
      case _: FileAlreadyExistsException =>
        logger.finer(s"File $filePath already exists."); false
    }
  }

  // Reads the current file as a map. Adds new things to add to this map and writes this entire map to file
  // overwriting the existing content.
  def addHashesAndContentOfLinesToPool(hashLineMap: mutable.LinkedHashMap[String, String],
                                       stringPoolFile: String,
                                       baseDirectory: String = ""): Unit = {
    logger.fine(
      s"Adding hashes & lines map, $hashLineMap to file ${baseDirectory + stringPoolFile}")
    var fileMap: mutable.LinkedHashMap[String, String] = getFileAsMap(
      baseDirectory + stringPoolFile)

    hashLineMap.foreach(tuple => {
      if (!fileMap.contains(tuple._1)) {
        fileMap += tuple
      }
    })
    logger.fine(
      s"After updating, $fileMap is now being written to ${baseDirectory + stringPoolFile}")

    // Once the map is populated, write the map to travelogue file
    writeMapToFile(fileMap, baseDirectory + stringPoolFile)
  }

  // Overwrites the existing content.
  def writeMapToFile(map: mutable.LinkedHashMap[String, String],
                     filePath: String,
                     append: Boolean = false): Unit = {
    logger.fine(s"Writing $map to file $filePath with append = $append")
    // printwriter empties the contents of a file if it exists
    val writer: PrintWriter = new PrintWriter(new FileWriter(filePath, append))
    map.foreach(tuple => writer.write(s"${tuple._1}:${tuple._2}\n"))
    writer.flush()
    writer.close()
  }

  // Overwrites the existing content.
  def addLineHashesToHashesFile(lineHashes: List[String], file: String): Unit = {
    logger.fine(s"Adding lines hashes $lineHashes to hashes file $file.")
    // printwriter empties the contents of a file if it exists
    val writer: PrintWriter = new PrintWriter(file)
    lineHashes.foreach(x => writer.write(s"$x\n"))
    writer.flush()
    writer.close()
  }

  // Reads the current file as a map. Adds new things to add to this map and writes this entire map to file
  // overwriting the existing content.
  def addToTravelogueFile(hashNameTuple: (String, String), baseDirectory: String = ""): Unit = {
    logger.fine(
      s"Trying to add tuple $hashNameTuple to travelogue file ${baseDirectory + TRAVELOGUE}")
    var map: mutable.LinkedHashMap[String, String] = getFileAsMap(TRAVELOGUE, baseDirectory)
    logger.fine(s"Travelogue file before adding: $map")

    // If the exact filePath -> fileHash exists in the map, Do nothing. But if not, it means the file has changed.
    // So we add in the current key value pair which just updates the value of existing key
    if (!map.exists(_ == (hashNameTuple._1 -> hashNameTuple._2))) {
      map += (hashNameTuple._1 -> hashNameTuple._2)
    }
    logger.fine(s"Travelogue file after adding/updating with the tuple $hashNameTuple: $map")

    // Once the map is populated, write the map to travelogue file
    writeMapToFile(map, TRAVELOGUE)
  }

  // copies file from src to dest
  def copyFile(src: String, dest: String): Path =
    Files.copy(Paths.get(src), Paths.get(dest), StandardCopyOption.REPLACE_EXISTING)

  def getFilesInThePitstop(pitstop: String, baseDirectory: String = ""): List[String] = {
    logger.fine(s"Trying to get the file in the pitstop $pitstop")
    val filesAndHashMap = getFileAsMap(PITSTOPS_FOLDER + pitstop, baseDirectory)
    logger.fine(s"Files in the pitstop $pitstop are ${filesAndHashMap.values.toList}")
    filesAndHashMap.values.toList
  }

  // returns the "filename: fileHash" file as a Map of (filename -> fileHash
  // OR
  // returns the lineHash: lineContent Map
  def getFileAsMap(filePath: String,
                   baseDirectory: String = ""): mutable.LinkedHashMap[String, String] = {
    logger.fine(s"Trying to get $filePath as a map.")
    var filenameHashMap: mutable.LinkedHashMap[String, String] =
      mutable.LinkedHashMap.empty
    if (!createIfDoesNotExist(baseDirectory + filePath)) {
      val fileLines = getLinesOfFile(filePath, baseDirectory)
      if (fileLines.nonEmpty) {
        fileLines.foreach(line => {
          // split(str, int) required to make sure that the splits array we get should have only two elements
          // at the max. Other wise, it splits at every instance of ":" and we will get a lot more things in
          // the splits array
          val splits = line.split(":", 2)
          filenameHashMap += (baseDirectory + splits(0) -> splits(1))
        })
      }
    }
    logger.fine(s"Map of the file $filePath = $filenameHashMap")
    filenameHashMap
  }

  def getLinesOfFile(filePath: String, baseDirectory: String = ""): List[String] = {
//    println(s"Filepath: $filePath, Base dire: $baseDirectory")
    lazy val source: BufferedSource = Source.fromFile(baseDirectory + filePath, "UTF-8")
    val lines: Try[List[String]] = Try(source.getLines().toList)
    if (lines.isSuccess) {
      source.close()
      lines.get
    } else {
      val bytes: Array[Byte] = Files.readAllBytes(Paths.get(baseDirectory + filePath))
      /*
       * Since mkString on the entire array can take a lot of time and might even give OOM errors,
       * We will take at max 100 elements in the array and create a string of that
       */
      val bytesString = bytes.take(100).mkString
      List(bytesString)
    }
  }

  // Try fails if its not able to read the file which happens if the file is binary file.
  // Not really the best way to do it but there doesn't seem to be any proper way to do it.
  def isBinaryFile(filePath: String): Boolean = {
    logger.fine(s"Checking if $filePath is a binary file")
    Try(Source.fromFile(filePath).mkString).isFailure
  }

  // fileName -> hash, coz hashes can be same but fileNames will always be different
  def getHashesOfAllFilesKnownToDelorean(baseDirectory: String = ""): Map[Path, String] = {
    var currentPitstop = getCurrentPitstop(baseDirectory)
    logger.fine(s"Current pitstop = $currentPitstop")
    getHashesOfAllFilesKnownToDelorean(currentPitstop, baseDirectory)
  }

  /**
    * Gets all the files known to delorean at a pitstop
    *
    * Basically it is the state of the repository at that particular pitstop
    */
  def getHashesOfAllFilesKnownToDelorean(pitStop: String,
                                         baseDirectory: String): Map[Path, String] = {
    // println(s"Pitstop: $pitStop, base directory: $baseDirectory")
    var map: Map[Path, String] = Map.empty
    var currentPitstop = pitStop
    while (currentPitstop.nonEmpty) {
      // fileName -> fileHash almost all of the places
      val pitstopMap = getFileAsMap(PITSTOPS_FOLDER + currentPitstop, baseDirectory)
      pitstopMap.foreach(
        kvPair =>
          if (!map.contains(Paths.get(kvPair._1).toAbsolutePath))
            map = map + (Paths.get(kvPair._1).toAbsolutePath -> kvPair._2)
      )
      currentPitstop = parent(currentPitstop, baseDirectory)
    }
    logger.fine(s"Files known from pitstops and their hashes: $map")

    // Apart from looking at the pitstops, also looks at the files in _temp file.
    // Those files shouldn't come in the untracked files too.
    val tempPitstopFile = getTempPitstopFileLocation(baseDirectory)
    if (tempPitstopFile.nonEmpty) {
      // fileName -> hash
      val tempFilePitstopMap = getFileAsMap(getTempPitstopFileLocation(baseDirectory))
      tempFilePitstopMap.foreach(kvPair => map = map + (Paths.get(kvPair._1) -> kvPair._2))
    }
    logger.fine(s"Updated map after looking at temp file too: $map")
    map
  }

  /**
    * Returns either the pitstop hash of the current commit or an empty String.
    */
  def getCurrentPitstop(baseDirectory: String = ""): String = {
    val currentPitstopOrTimeline: String = getLinesOfFile(CURRENT_INDICATOR, baseDirectory).head
    // If there is a timeline name in the 'current' file. We return the pitstop present in the timeline
    if (Files.exists(Paths.get(baseDirectory + INDICATORS_FOLDER + currentPitstopOrTimeline))) {
      val lines: List[String] =
        getLinesOfFile(INDICATORS_FOLDER + currentPitstopOrTimeline, baseDirectory)
      if (lines.nonEmpty) lines.head else ""
    } else {
      // If there is a pitstop hash instead of a branch name we just return the 'pitstop' hash
      currentPitstopOrTimeline
    }
  }

  // Gets the parent pitstop of the given pitstop. Would be an empty string if no parent is present.
  def parent(pitstop: String, baseDirectory: String = ""): String = {
    val parent: String = getLinesOfFile(METADATA_FOLDER + pitstop, baseDirectory)
      .filter(_.contains("Parent"))
      .head
      .split(":", 2)(1)
    logger.finer(s"Parent of $pitstop is $parent")
    parent
  }

  // sends the full path something like .tm/pitstops/_temp...
  def getTempPitstopFileLocation(baseDirectory: String = ""): String = {
    val tempFileArray: Array[File] =
      filesMatchingInDir(new File(baseDirectory + PITSTOPS_FOLDER), _.startsWith("_temp"))
    if (tempFileArray.nonEmpty) tempFileArray(0).getPath else ""
  }

  def filesMatchingInDir(dir: File, check: String => Boolean): Array[File] = {
    dir.listFiles(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = check(name)
    })
  }

  def getAllDeloreanTrackedFiles(baseDirectory: String = ""): List[String] =
    getFileAsMap(TRAVELOGUE, baseDirectory).keys.toList

}
