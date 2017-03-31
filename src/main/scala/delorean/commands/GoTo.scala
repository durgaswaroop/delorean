/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean.commands

import java.io.File
import java.nio.file.{Files, Paths}
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

    val (isTimeLine, isPitstop) = if (new File(INDICATORS_FOLDER + timeLine).exists) (true, false) else (false, true)

    /*
        If 'timeline' is in-fact a timeline we get the pitstop that timeline is pointing to or else timeline is a pitstop hash
        which we get by resolving the full pitstop
    */
    val pitstopToGoTo: String = if (isTimeLine) resolveTheHashOfTimeline(timeLine) else resolveTheCorrectPitstop(timeLine)
    logger.fine(s"Pitstop to goto: $pitstopToGoTo")

    val pitstopFileMap: mutable.LinkedHashMap[String, String] = getFileAsMap(PITSTOPS_FOLDER + pitstopToGoTo)
    pitstopFileMap foreach (kv ⇒ {
        val (fileKnown, hashKnown) = kv
        logger.fine(s"(${kv._1}, ${kv._2})")

        // If file exists we check if the current hash is same as the hash we have saved
        if (Files.exists(Paths.get(fileKnown))) {
            val hashComputed = computeShaHash(fileKnown)
            logger.fine(s"computed hash = $hashComputed")

            // If the hashes are not same, we replace the existing file with the file from the pitstop we are 'going to'
            if (hashComputed != hashKnown) Reconstruct.file(fileKnown, hashKnown)
        }
        else {
            logger.fine(s"File $fileKnown does not exist in the current repository")
            Files.createFile(Paths.get(fileKnown))
            Reconstruct.file(fileKnown, hashKnown)
        }
    })

    /*
        Once everything is done, we need to update the 'current' indicator.
        Whether its called with a timeline or a pitstop hash, once we 'goto' that, we will add that in the current file.
     */
    writeToFile(CURRENT_INDICATOR, timeLine)
}
