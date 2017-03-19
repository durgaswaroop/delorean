package delorean
package commands

import java.io.File
import java.nio.file.{Files, Path, Paths}
import java.util.function.Predicate

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
    // If the 'current' file is pointing to a timeline
    if (Files.exists(Paths.get(INDICATORS_FOLDER + currentTimeline))) {
        println(s"On timeline '$currentTimeline'\n")
    } else {
        //If its not a timeline it will be a pitstop
        println(
            s"""
               |On pitstop '$currentTimeline'
               |
               |You are not on any timeline now. To goto an existing timeline, run
               |    delorean goto <timeline>
               |
               |For more: delorean --help
            """.stripMargin)
    }

    val tempFiles: Array[File] = filesMatchingInDir(new File(PITSTOPS_FOLDER), _.startsWith("_temp"))
    var addedFileSet: List[String] = List("")
    if (tempFiles.length > 0) {
        addedFileSet = getFileAsMap(tempFiles.head.getPath).values.toList
        println("Files ready to be added to a pitstop:")
        println(addedFileSet.mkString("\tModified: ", "\n\tModified: ", "\n"))
    }

    val allFilesAndHashesKnownToDelorean: Map[String, String] = getHashesOfAllFilesKnownToDelorean
    val modifiedFiles: List[String] = getModifiedFiles.filterNot(_.isEmpty)

    if (modifiedFiles.nonEmpty) {
        println(
            """Files modified since last pitstop:
              | (use "delorean add <filename>" to stage the changes for the next pitstop
            """.stripMargin)
        println(modifiedFiles.mkString("\tModified: ", "\n\tModified: ", "\n"))
    }

    val untrackedFiles: List[String] = getUntrackedFiles.filterNot(_.isEmpty)

    if (untrackedFiles.nonEmpty) {
        println(
            """Untracked files:
              | (use "delorean add <filename>" to stage the file to be added to the next pitstop
            """.stripMargin)
        println(untrackedFiles.mkString("\t", "\n\t", ""))
    }

    def getModifiedFiles: List[String] = {
        val hasher = new Hasher
        val allFiles: Iterable[String] = allFilesAndHashesKnownToDelorean.values
        var modifiedFiles: List[String] = List("")
        allFiles.foreach(file ⇒ {
            val hash = hasher.computeHashOfFile(file, justGetTheHash = true)
            if (!allFilesAndHashesKnownToDelorean.exists(_ == (hash, file))) modifiedFiles = file :: modifiedFiles
        })
        modifiedFiles
    }

    def getUntrackedFiles: List[String] = {
        val allFilesDeloreanKnows: List[String] = allFilesAndHashesKnownToDelorean.values.toList

        // ".tm" directory should be ignored always
        val biffFileContents: List[String] = ".tm" :: getLinesOfFile(IGNORE_FILE)
        var ignored = List[String]()
        biffFileContents.foreach(f ⇒ if (Files.isDirectory(Paths.get(f))) ignored = ignored ::: getFilesRecursively(f))

        val predicate: Predicate[Path] = {
            p: Path ⇒
                val pathString = p.normalize.toString
                !allFilesDeloreanKnows.contains(pathString) && !ignored.contains(pathString)
        }

        val prefix = "."
        val allFilesInDirectory: List[String] = getFilesRecursively(prefix, predicate)
        allFilesInDirectory
    }

}