/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean.commands

import java.io.File
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
            s"""Pitstop ${correctPitstop take 20}
               |Rider: ${metadata.rider}
               |Time: ${metadata.time.format(DateTimeFormatter.ofPattern("MMM dd yyyy hh:mm a zz"))}
               |Parent(s): ${metadata.parents.mkString(", ")}
               |Rider Log: ${metadata.riderLog}
               |Changes: ${changedFilesMap.keys.mkString("\n\t", "\n\t", "")}
             """.stripMargin)
    }

    // Gets the full pitstop hash from the first few characters given
    def resolveTheCorrectPitstop(simplifiedPitstop: String): String = {
        val files: Array[File] = filesMatchingInDir(new File(PITSTOPS_FOLDER), _ startsWith simplifiedPitstop)
        if (files.length > 1) {
            println(
                s"""Ambiguous pitstop hash $simplifiedPitstop
                   |Found multiple pitstops matching this hash.
                """.stripMargin)
            "" // return empty string if more than one hash is found starting with the given characters
        } else if (files.length == 0) {
            println(s"Pitstop $simplifiedPitstop not found in the current repository")
            "" // return empty string if no hashes are found
        }
        else files.head.getName
    }
}
