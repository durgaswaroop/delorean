package delorean
package commands

import java.io.File

import delorean.FileOps._

/**
  * Class for the command 'status'
  */
class Status {
    val currentPitstop: String = getLinesOfFile(CURRENT_INDICATOR).head
    println(s"On pitstop ${currentPitstop take 10}")
    val tempFiles: Array[File] = filesMatchingInDir(new File(PITSTOPS_FOLDER), _.startsWith("_temp"))
    if (tempFiles.length > 0) {
        val addedFileSet: Iterable[String] = getFileAsMap(tempFiles.head.getPath).values
        println("Files ready to be added to a pitstop:")
        println("\n\t" + addedFileSet.mkString + "\n")
    }
    println("Files modified since last pitstop:")
    println("""  (use "delorean add <filename>" to stage the changes for the next pitstop""" + "\n")
    //TODO: Should print the list of files that are modified since the last pitstop
    println("Files untracked:")
    //TODO: Should print the list of all the files in the repository except for the ones already added/pitstopped
}
