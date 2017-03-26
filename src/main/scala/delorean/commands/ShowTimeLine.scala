package delorean
package commands

import delorean.FileOps._
import delorean.commands.OutputFormat.OutputFormat

/**
  * Class for the command 'show-timeline'.
  *
  */
case class ShowTimeLine(outputFormat: OutputFormat = OutputFormat.SHORT) {

    // Either the pitstop hash or an empty string
    val currentPitstop: String = getCurrentPitstop

    // If current pitstop is empty it means there are no pitstops shown yet.
    if (currentPitstop.isEmpty) {
        println("delorean: No pitstops present on the current timeline")
        System.exit(1)
    }

    printTimeLine(currentPitstop, outputFormat)
    var parentPitstop: String = parent(currentPitstop)
    while (parentPitstop nonEmpty) {
        printTimeLine(parentPitstop, outputFormat)
        parentPitstop = parent(parentPitstop)
    }

    def printTimeLine(pitstop: String, format: OutputFormat): Unit = {
        val metadata = Metadata(pitstop)
        if (format == OutputFormat.SHORT) println("* " + pitstop.take(6) + " " + metadata.riderLog.take(60))
        else println(
            s"""pitstop ${pitstop.take(25)}
               |Rider: ${metadata.rider}
               |Time:  ${metadata.time}
               |
               |Rider log: ${metadata.riderLog}
                """.stripMargin)
    }
}

object OutputFormat extends Enumeration {
    type OutputFormat = Value
    val SHORT = Value(0)
    val LONG = Value(1)
}