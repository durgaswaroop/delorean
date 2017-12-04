/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

import java.io.File
import java.nio.file.{Files, Path, Paths}

import delorean.FileOps.{filesMatchingInDir, getFilesRecursively, getLinesOfFile, logger}

import scala.collection.mutable.ListBuffer

/**
  * Package object to hold variables.
  */
package object delorean {
  val TIME_MACHINE: String = ".tm/"

  val INDICATORS_FOLDER: String = TIME_MACHINE + "indicators/"
  val PITSTOPS_FOLDER: String = TIME_MACHINE + "pitstops/"
  val HASHES_FOLDER: String = TIME_MACHINE + "hashes/"
  val METADATA_FOLDER: String = TIME_MACHINE + "metadata/"
  val BINARIES_FOLDER: String = TIME_MACHINE + "bins/"

  val CONFIG: String = TIME_MACHINE + "config"
  val STRING_POOL: String = TIME_MACHINE + "string_pool"
  val TRAVELOGUE: String = TIME_MACHINE + "travelogue"
  val CURRENT_INDICATOR: String = INDICATORS_FOLDER + "current"
  val DEFAULT_TIMELINE: String = INDICATORS_FOLDER + "present"

  val IGNORE_FILE: String = ".biff"

  val DELOREAN_SERVER_PORT: Int = 18987

  /**
    * Gets the full pitstop hash from the first few characters given
    *
    * @param simplifiedPitstop : pitstop to resolve
    * @return : Full pitstop hash if its present or else an empty string
    */
  def resolveTheCorrectPitstop(simplifiedPitstop: String): String = {
    val files: Array[File] =
      filesMatchingInDir(new File(PITSTOPS_FOLDER), _ startsWith simplifiedPitstop)
    if (files.length > 1) {
      println(s"""Ambiguous pitstop hash $simplifiedPitstop
                   |Found multiple pitstops matching this hash.
                """.stripMargin)
      "" // return empty string if more than one hash is found starting with the given characters
    } else if (files.length == 0) {
      println(s"Pitstop $simplifiedPitstop not found in the current repository")
      "" // return empty string if no hashes are found
    } else files.head.getName
  }

  /**
    * Gets the pitstop hash of the given timeline
    *
    * @param timeLine      : timeline to get the hash for
    * @param baseDirectory : Directory to start the search in
    * @return : Pitstop hash of the timeline if it is non-empty or else an empty string
    */
  def resolveTheHashOfTimeline(timeLine: String, baseDirectory: String = ""): String = {
    val fileLines =
      FileDictionary(baseDirectory + INDICATORS_FOLDER + timeLine, linesNeeded = true).lines
    if (fileLines.nonEmpty) fileLines.head else ""
  }

  /**
    * Get files that are not part of the repository.
    *
    * @return : A Set of untracked file names
    */
  def getUntrackedFiles(baseDirectory: String = ""): Set[String] = {
    val ignoredFiles: Set[Path] = getIgnoredFiles(baseDirectory).toSet
    logger.fine(s"Ignored files: $ignoredFiles")
    val allFilesDeloreanKnows: Set[Path] =
      FileOps.getHashesOfAllFilesKnownToDelorean(baseDirectory).keys.toSet
    logger.fine(s"All files Dolorean knows: $allFilesDeloreanKnows")
    val allFilesInMainDirectory: Set[Path] =
      getFilesRecursively(if (baseDirectory.isEmpty) "." else baseDirectory)
        .map(x => Paths.get(x))
        .filterNot(p => p.toFile.isDirectory)
        .toSet
    logger.fine(s"All files in main directory: $allFilesInMainDirectory")
    val untrackedFiles: Set[Path] = allFilesInMainDirectory -- allFilesDeloreanKnows -- ignoredFiles
    logger.fine(s"Untracked files: $untrackedFiles")
    untrackedFiles.map(_.toAbsolutePath.toString)
  }

  /**
    * Get files that are ignored.
    *
    * @return : A Set of ignored file names
    */
  def getIgnoredFiles(baseDirectory: String = ""): List[Path] = {
    // ".tm" directory should be ignored always
    val biffFileContents: Set[String] = Set(baseDirectory + TIME_MACHINE) ++ {
      if (new File(baseDirectory + IGNORE_FILE).exists())
        getLinesOfFile(baseDirectory + IGNORE_FILE).toSet
      else Set.empty[String]
    }
    logger.fine(s"biffFileContents : $biffFileContents")

    val ignoredFiles: ListBuffer[Path] = ListBuffer(
      Paths.get(baseDirectory + TIME_MACHINE).toAbsolutePath)
    biffFileContents.foreach { path =>
      {
        if (Files.isDirectory(Paths.get(path))) {
          ignoredFiles ++= getFilesRecursively(path).map(Paths.get(_))
        } else {
          ignoredFiles += Paths.get(path).toAbsolutePath
        }
      }
    }

    // logger.fine(s"Ignored files: $ignoredFiles")
    ignoredFiles.toList
  }

  def isDeloreanRepo: Boolean = {
    Files.exists(Paths.get(TIME_MACHINE)) &&
    Files.exists(Paths.get(PITSTOPS_FOLDER)) &&
    Files.exists(Paths.get(HASHES_FOLDER)) &&
    Files.exists(Paths.get(METADATA_FOLDER)) &&
    Files.exists(Paths.get(INDICATORS_FOLDER)) &&
    Files.exists(Paths.get(BINARIES_FOLDER)) &&
    Files.exists(Paths.get(CONFIG)) &&
    Files.exists(Paths.get(CURRENT_INDICATOR)) &&
    Files.exists(Paths.get(DEFAULT_TIMELINE)) &&
    Files.exists(Paths.get(STRING_POOL)) &&
    Files.exists(Paths.get(TRAVELOGUE))
  }
}
