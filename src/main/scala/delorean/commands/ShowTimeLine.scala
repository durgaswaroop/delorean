package delorean
package commands

import delorean.FileOps._

/**
  * Class for the command 'show-timeline'.
  */
class ShowTimeLine {
    val currentIndicatorFileLines: List[String] = getLinesOfFile(CURRENT_INDICATOR)

    if (currentIndicatorFileLines.isEmpty) {
        println("On timeline - default timeline ")
        println("No pitstops found in the repository.\nFor more information: delorean --help")
    }
    else {
        var currentPitstop: String = currentIndicatorFileLines.head
        println("* " + currentPitstop.take(6))

        var parentPitstop = parent(currentPitstop)
        while (parentPitstop nonEmpty) {
            println("* " + parentPitstop.take(6))
            parentPitstop = parent(parentPitstop)
        }
    }

    def parent(pitstop: String): String = {
        val parent: String = getLinesOfFile(METADATA_FOLDER + pitstop).filter(_.contains("Parent")).head.split(":", 2)(1)
        // if (parent.nonEmpty) println("* " + parent.take(6))
        parent
    }
}
