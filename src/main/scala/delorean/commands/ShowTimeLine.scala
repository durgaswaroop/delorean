/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

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

  /**
    * Prints the timeline information in ethe given 'format'.
    *
    * If format is SHORT, we will just see the pitstop hash and the rider log
    * If the format is Long, we will see that pitstop's metadata as well.
    *
    * @param pitstop : pitstop to print
    * @param format  : OutputFormat. Can be SHORT or LONG
    */
  def printTimeLine(pitstop: String, format: OutputFormat): Unit = {
    val metadata = Metadata(pitstop)
    if (format == OutputFormat.SHORT)
      println("* " + pitstop.take(6) + " " + metadata.riderLog.take(60))
    else println(s"""pitstop ${pitstop.take(25)}
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
