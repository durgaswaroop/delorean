/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean.commands

import java.time.format.DateTimeFormatter

import delorean.FileOps._
import delorean._

/**
  * Class for 'describe' command.
  */
case class Describe(pitstops: List[String]) {
    pitstops.foreach(pitstop ⇒ {
        docExplain(pitstop)
        if (pitstops.length > 1) println
    })

    // Let the doc explain the pitstop
    def docExplain(simplifiedPitstop: String): Unit = {
        val correctPitstop = resolveTheCorrectPitstop(simplifiedPitstop)
        if (correctPitstop.isEmpty) return
        val metadata: Metadata = Metadata(correctPitstop)

        // fileName -> fileHash
        val changedFilesMap = getFileAsMap(PITSTOPS_FOLDER + correctPitstop)
        println(
            s"""Pitstop $correctPitstop
               |Rider: ${metadata.rider}
               |Time: ${metadata.time.format(DateTimeFormatter.ofPattern("MMM dd yyyy hh:mm a zz"))}
               |Parent(s): ${metadata.parents.mkString(", ")}
               |Rider Log: ${metadata.riderLog}
               |Changes: ${changedFilesMap.keys.mkString("\n\t", "\n\t", "")}
             """.stripMargin)
    }
}
