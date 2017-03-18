package delorean
package commands

import delorean.FileOps._
import delorean.commands.OutputFormat.OutputFormat

/**
  * Class for the command 'show-timeline'.
  *
  */
case class ShowTimeLine(outputFormat: OutputFormat = OutputFormat.SHORT) {

    val currentPitstop: String = getCurrentPitstop
    if (outputFormat == OutputFormat.SHORT) {
        printShort(currentPitstop)
        var parentPitstop = parent(currentPitstop)
        while (parentPitstop nonEmpty) {
            printShort(parentPitstop)
            parentPitstop = parent(parentPitstop)
        }
    } else {
        printLong(currentPitstop)
        var parentPitstop = parent(currentPitstop)
        while (parentPitstop nonEmpty) {
            printLong(parentPitstop)
            parentPitstop = parent(parentPitstop)
        }
    }

    def printShort(pitstop: String): Unit = {
        val metadata = Metadata(pitstop)
        println("* " + pitstop.take(6) + " " + metadata.riderLog.take(60))
    }

    def printLong(pitstop: String): Unit = {
        val metadata = Metadata(pitstop)
        println(
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