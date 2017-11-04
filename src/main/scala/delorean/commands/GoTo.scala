/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean
package commands

import java.io.File
import java.nio.file.{Files, Path, Paths}
import java.util.logging.Logger

import delorean.FileOps._
import delorean.Hasher.computeShaHash
import delorean._

import scala.collection.mutable

/**
  * Class for the goto command.
  * The input can be a timeline or a pitstop
  */
case class GoTo(timeLine: String) {
    val logger: Logger = Logger.getLogger(this.getClass.getName)

    val isTimeLine = new File(INDICATORS_FOLDER + timeLine).exists
    val isPitstop = resolveTheCorrectPitstop(timeLine).nonEmpty

    (isTimeLine, isPitstop) match {
        case (true, false) => goToTimeline(timeLine)
        case (false, true) => goToPitstop(resolveTheCorrectPitstop(timeLine))
        case _ =>
    }

    def goToTimeline(timeLine: String): Unit = {
        /*
       If 'timeline' is in-fact a timeline we get the pitstop that timeline is pointing to or else timeline is a pitstop hash
       which we get by resolving the full pitstop
   */
        val pitstopToGoTo: String = resolveTheHashOfTimeline(timeLine)
        goToPitstop(pitstopToGoTo)
        // Update the current indicator
        writeToFile(CURRENT_INDICATOR, timeLine)
    }

    def goToPitstop(pitstopToGoTo: String): Unit = {
        logger.info(s"Pitstop to goto: $pitstopToGoTo")

        val allDirectoryFiles: List[String] = getFilesRecursively(".")
        logger.info(s"\nAll directory files $allDirectoryFiles")

        // Names and hashes of all the files in the repo at the pitstop
        val pitstopFileMap: Map[Path, String] = getHashesOfAllFilesKnownToDelorean(pitstopToGoTo)
        logger.info(s"\nPitstop file map: $pitstopFileMap")

        val ignoredFiles: List[String] = getIgnoredFiles.toList.map(_.toString)
        logger.info(s"\nIgnored files: $ignoredFiles")

        // 1. Delete all the delorean tracked files that are in the current directory but not in the 'goto'
        // destination pitstop
        val filesToDelete: List[String] = (allDirectoryFiles diff ignoredFiles diff pitstopFileMap.keys.toList).filter(_.nonEmpty)
        logger.info(s"\nFiles to delete: $filesToDelete")
        filesToDelete foreach (file => {
            logger.info(s"Deleting file '$file'")
            Files.delete(Paths.get(file))
        })

        // 2. Rest all files that are present in the current directory and in the 'goto' pitstop to their state at that
        //  pitstop
        pitstopFileMap foreach (kv ⇒ {
            val (fileKnown, hashKnown) = kv
            logger.fine(s"(${kv._1}, ${kv._2})")

            // If file exists we check if the current hash is same as the hash we have saved
            if (Files.exists(fileKnown)) {
                val hashComputed = computeShaHash(fileKnown.toString)
                logger.fine(s"computed hash = $hashComputed")

                // If the hashes are not same, we replace the existing file with the file from the pitstop we are 'going to'
                if (hashComputed != hashKnown) Reconstruct.file(fileKnown.toString, hashKnown)
            }
            else {
                logger.fine(s"File $fileKnown does not exist in the current repository")
                Files.createFile(fileKnown)
                Reconstruct.file(fileKnown.toString, hashKnown)
            }
        })
        // Update the current indicator
        writeToFile(CURRENT_INDICATOR, pitstopToGoTo)
    }

}
