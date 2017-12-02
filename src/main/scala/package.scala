/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

import java.io.File
import java.nio.file.{Files, Path, Paths}

import delorean.FileOps.{
  filesMatchingInDir,
  getFilesRecursively,
  getLinesOfFile,
  logger
}

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

  val GIT_SERVER_PORT: Int = 18987

  /**
    * Gets the full pitstop hash from the first few characters given
    *
    * @param simplifiedPitstop : pitstop to resolve
    * @return : Full pitstop hash if its present or else an empty string
    */
  def resolveTheCorrectPitstop(simplifiedPitstop: String): String = {
    val files: Array[File] = filesMatchingInDir(new File(PITSTOPS_FOLDER),
                                                _ startsWith simplifiedPitstop)
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
    * @param timeLine : timeline to get the hash for
    * @return : Pitstop hash of the timeline if it exists or else an empty string
    */
  def resolveTheHashOfTimeline(timeLine: String): String = {
    val timeLineFile = new File(INDICATORS_FOLDER + timeLine)
    if (timeLineFile.exists())
      FileDictionary(INDICATORS_FOLDER + timeLine, linesNeeded = true).lines.head
    else ""
  }

  /**
    * Get files that are not part of the repository.
    *
    * @return : A Set of untracked file names
    */
  def getUntrackedFiles: Set[String] = {
    val ignoredFiles: Set[Path] = getIgnoredFiles
    val allFilesDeloreanKnows: Set[Path] =
      FileOps.getHashesOfAllFilesKnownToDelorean.keys.toSet
    val allFilesInMainDirectory: Set[Path] =
      getFilesRecursively(".")
        .map(x => Paths.get(x))
        .filterNot(p => p.toFile.isDirectory)
        .toSet
    val untrackedFiles
      : Set[Path] = allFilesInMainDirectory -- allFilesDeloreanKnows -- ignoredFiles
    logger.fine(s"Untracked files: $untrackedFiles")
    untrackedFiles.map(_.toString)
  }

  /**
    * Get files that are ignored.
    *
    * @return : A Set of ignored file names
    */
  def getIgnoredFiles: Set[Path] = {
    // ".tm" directory should be ignored always
    val biffFileContents: Set[String] = Set(".tm") ++ {
      if (new File(IGNORE_FILE).exists()) getLinesOfFile(IGNORE_FILE).toSet
      else Set.empty[String]
    }
    logger.fine(s"biffFileContents : $biffFileContents")

    var ignoredFiles: Set[Path] = Set.empty
    biffFileContents.foreach { path =>
      {
        if (Files.isDirectory(Paths.get(path))) {
          ignoredFiles ++= getFilesRecursively(path).map(Paths.get(_)).toSet
        } else {
          ignoredFiles += Paths.get(path)
        }
      }
    }
    logger.fine(s"Ignored files: $ignoredFiles")
    // TODO: For some reason this also returns an empty path. For now putting a filter like this. Have to take a look
    ignoredFiles.filter(_.toString.nonEmpty)
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
