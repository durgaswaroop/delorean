/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean.commands

import java.nio.file.{Files, Paths}
import java.util.logging.Logger

import delorean.FileOps._
import delorean._

/**
  * Class for create-timeline command.
  */
case class CreateTimeLine(newTimeLine: String) {

    val logger: Logger = Logger.getLogger(this.getClass.getName)

    logger.fine(s"New timeline to create: $newTimeLine")

    val newTimeLineFileLocation: String = INDICATORS_FOLDER + newTimeLine

    // First check if that timeline already exists
    if (Files.exists(Paths.get(newTimeLineFileLocation))) {
        println(s"Timeline '$newTimeLine' already exists in the current repository")
        println("Science Fiction 101: You cannot 're-create' an existing timeline")
        System.exit(1)
    }

    // If the timeline doesn't already exist, create a file with the name of the new timeline in the Indicators folder
    Files.createFile(Paths.get(newTimeLineFileLocation))

    // Then, copy the current pitstop hash (the contents of the indicator file) to the current timeline file
    val currentPitstop: String = getCurrentPitstop
    writeToFile(newTimeLineFileLocation, currentPitstop)

    // Then, point the 'current' branch pointer to point to the new branch
    writeToFile(CURRENT_INDICATOR, newTimeLine)

    println(s"New timeline $newTimeLine created")
}
