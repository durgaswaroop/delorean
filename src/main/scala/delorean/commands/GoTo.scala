/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean
package commands

import java.nio.file.{Files, Path, Paths}
import java.util.logging.Logger

import delorean.FileOps._
import delorean.Hasher.computeShaHash

/**
  * Class for the goto command.
  * The input can be a timeline or a pitstop
  */
case class GoTo(timeLine: String, baseDirectory: String = "") {
  val logger: Logger = Logger.getLogger(this.getClass.getName)
  logger.fine(s"Timeline: $timeLine, Base direc: $baseDirectory")

  logger.fine(baseDirectory + INDICATORS_FOLDER + timeLine)

  // If it is indeed a timeline
  if (Files.exists(Paths.get(baseDirectory + INDICATORS_FOLDER + timeLine)))
    goToTimeline(timeLine)
  // If it is a pitstop
  else if (resolveTheCorrectPitstop(timeLine).nonEmpty)
    goToPitstop(resolveTheCorrectPitstop(timeLine))
  else {} // If it is neither, do nothing

  def goToTimeline(timeLine: String): Unit = {
    /*
     *If 'timeline' is in-fact a timeline we get the pitstop that timeline is pointing to or else timeline is a pitstop hash
     *which we get by resolving the full pitstop
     */
    val pitstopToGoTo: String = resolveTheHashOfTimeline(timeLine, baseDirectory)

    if (pitstopToGoTo isEmpty) {
      println("delorean: Timeline 'present' does not have any commits yet")
      return
    }
    updateRepoToCommit(pitstopToGoTo)
    // Update the current indicator
    writeToFile(baseDirectory + CURRENT_INDICATOR, timeLine)
    println(s"Moved to timeline '$timeLine'")
  }

  def goToPitstop(pitstopToGoTo: String): Unit = {
    updateRepoToCommit(pitstopToGoTo)
    // Update the current indicator
    writeToFile(CURRENT_INDICATOR, pitstopToGoTo)
    println(s"""|Moved to pistop '${pitstopToGoTo.take(6)}'.
                |Currently not on any timeline.
                |
                |NOTE: Not on any timeline now. To goto an existing timeline, run
                |    delorean goto <timeline>
                |
                |To create a new timeline at this pitstop:
                |    delorean create-timeline <timeline>
            """.stripMargin)
  }

  /**
    * Updates the repository to the same state as it was at the given pitstop.
    */
  def updateRepoToCommit(pitstopToGoTo: String): Unit = {
    logger.info(s"Pitstop to goto: $pitstopToGoTo")

    val directory = if (baseDirectory.isEmpty) "." else baseDirectory
    // Get files in the current directory if no base directory is given else get it from that directory
    val allDirectoryFiles: List[String] = getFilesRecursively(directory)

    logger.info(s"\nAll directory files $allDirectoryFiles")

    // Names and hashes of all the files in the repo at the pitstop
    val pitstopFileMap: Map[Path, String] =
      getHashesOfAllFilesKnownToDelorean(pitstopToGoTo, baseDirectory)
    val pitstopFiles = pitstopFileMap.keys.map(_.toAbsolutePath.toString).toList
    logger.info(s"\nPitstop file map: $pitstopFileMap")
    logger.info(s"\nPitstop Files: $pitstopFiles")

    val ignoredFiles: List[String] = getIgnoredFiles(baseDirectory).map(_.toString)
    logger.info(s"\nIgnored files: $ignoredFiles")

    val untrackedFiles: List[String] =
      getUntrackedFiles(baseDirectory).toList
    logger.info(s"Untracked files: $untrackedFiles")

    // 1. Delete all the delorean tracked files that are in the current directory but not in the 'goto'
    // destination pitstop
    val filesToDelete: List[String] =
      (allDirectoryFiles diff ignoredFiles diff untrackedFiles diff pitstopFiles)
        .filterNot(_ == baseDirectory)
        .filter(_.nonEmpty)
    logger.info(s"\nFiles to delete: $filesToDelete")
    filesToDelete foreach (file => {
      logger.info(s"Deleting file '$file'")
      Files.delete(Paths.get(file))
    })

    // 2. Rest all files that are present in the current directory and in the 'goto' pitstop to their state at that
    //  pitstop
    pitstopFileMap foreach (kv => {
      val (fileKnown, hashKnown) = kv
      logger.fine(s"($fileKnown, $hashKnown)")

      // If file exists we check if the current hash is same as the hash we have saved
      if (Files.exists(fileKnown)) {
        val hashComputed = computeShaHash(fileKnown.toString)
        logger.fine(s"computed hash = $hashComputed")

        // If the hashes are not same, we replace the existing file with the file from the pitstop we are 'going to'
        if (hashComputed != hashKnown)
          Reconstruct.file(fileKnown.toString, hashKnown, baseDirectory)
      } else {
        logger.fine(s"File $fileKnown does not exist in the current repository")
        Files.createFile(fileKnown)
        Reconstruct.file(fileKnown.toString, hashKnown, baseDirectory)
      }
    })
  }

}
