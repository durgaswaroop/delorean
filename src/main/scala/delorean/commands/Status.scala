package delorean
package commands

import java.io.File
import java.nio.file.{Files, Paths}

import delorean.FileOps._

/**
  * Class for the command 'status'
  */
class Status {
    if (!Files.exists(Paths.get(TIME_MACHINE))) {
        println(
            """
              |delorean: There is no repository in this directory. Check your current directory and try again.
              |
              |For more: delorean --help
            """.stripMargin)
        System.exit(0)
    }

    // Gets the name of the current timeline. Default timeline is "present"
    val currentTimeline: String = getLinesOfFile(CURRENT_INDICATOR).head
    if (currentTimeline.nonEmpty) {
        println(s"On timeline $currentTimeline")
    } else {
        // For when you're not on a timeline but directly did a 'goto' on a pitstop.
        val currentPitstop: String = getLinesOfFile(INDICATORS_FOLDER + currentTimeline).mkString
        if (currentPitstop.nonEmpty) {
            println(s"On pitstop ${currentPitstop take 10}")
        } else {
            println("No pitstops present")
        }
    }

    val tempFiles: Array[File] = filesMatchingInDir(new File(PITSTOPS_FOLDER), _.startsWith("_temp"))
    if (tempFiles.length > 0) {
        val addedFileSet: List[String] = getFileAsMap(tempFiles.head.getPath).values.toList
        println("Files ready to be added to a pitstop:")
        println("\n\t" + addedFileSet.mkString + "\n")
    }
    println("Files modified since last pitstop:")
    println("""  (use "delorean add <filename>" to stage the changes for the next pitstop""" + "\n")
    //TODO: Should print the list of files that are modified since the last pitstop
    println("Files untracked:")
    //TODO: Should print the list of all the files in the repository except for the ones already added/pitstopped
}