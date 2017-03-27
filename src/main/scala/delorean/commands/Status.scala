/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean
package commands

import java.io.File
import java.nio.file.{Files, Path, Paths}

import delorean.FileOps._

/**
  * Class for the command 'status'
  */
case class Status(fileName: String = "") {
    if (!Files.exists(Paths.get(TIME_MACHINE))) {
        println(
            """
              |delorean: There is no repository in this directory. Check your current directory and try again.
              |
              |For more: delorean --help
            """.stripMargin)
        System.exit(0)
    }

    val allFilesAndHashesKnownToDelorean: Map[Path, String] = getHashesOfAllFilesKnownToDelorean

    if (fileName.nonEmpty) {
        logger.fine(s"Status requested for file $fileName")
        val hashOfLastKnownVersionOfFile = allFilesAndHashesKnownToDelorean(Paths.get(fileName))
        val hasher = new Hasher
        if (hasher.computeFileHash(fileName, justGetTheHash = true) == hashOfLastKnownVersionOfFile)
            println(s"file $fileName has not been modified since the last pitstop")
        else
            println(s"file $fileName is different from the last pitstopped/staged version")
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

    val tempFile: String = getTempPitstopFileLocation
    var stagedFileSet: List[String] = List("")
    if (tempFile nonEmpty) {
        stagedFileSet = getFileAsMap(tempFile).keys.toList
        val newlyStagedFiles = stagedFileSet diff allFilesAndHashesKnownToDelorean.keys.toList
        val changedFiles = stagedFileSet diff newlyStagedFiles
        println(
            """Files ready to be added to a pitstop:
              | (use "delorean pitstop -rl <rider log>" to make a pitstop)
            """.stripMargin)
        if (newlyStagedFiles nonEmpty) println(newlyStagedFiles.sorted.mkString("\tNew: ", "\n\tNew: ", "\n"))
        if (changedFiles nonEmpty) println(changedFiles.sorted.mkString("\tModified: ", "\n\tModified: ", "\n"))
    }

    val modifiedAndDeletedFiles: (List[String], List[String]) = getModifiedAndDeletedFiles
    val modifiedFiles: List[String] = modifiedAndDeletedFiles._1.filterNot(_.isEmpty)
    val deletedFiles: List[String] = modifiedAndDeletedFiles._2.filterNot(_.isEmpty)

    if (modifiedFiles.nonEmpty || deletedFiles.nonEmpty) {
        println(
            """Files modified since last pitstop:
              | (use "delorean stage <filename>" to stage the changes for the next pitstop)
            """.stripMargin)
        if (modifiedFiles.nonEmpty) println(modifiedFiles.sorted.mkString("\tModified: ", "\n\tModified: ", "\n"))
        if (deletedFiles.nonEmpty) println(deletedFiles.sorted.mkString("\tDeleted: ", "\n\tDeleted: ", "\n"))
    }

    val untrackedFiles: Set[String] = getUntrackedFiles.filterNot(_.isEmpty)

    if (untrackedFiles.nonEmpty) {
        println(
            """Untracked files:
              | (use "delorean stage <filename>" to stage the file to be added to the next pitstop)
            """.stripMargin)
        println(untrackedFiles.toList.sorted.mkString("\t", "\n\t", ""))
    }

    // If a file is present in the 'filesKnownToDelorean' but is not currently there, it means that it is deleted.
    def getModifiedAndDeletedFiles: (List[String], List[String]) = {
        val hasher = new Hasher
        val allFiles: Iterable[Path] = allFilesAndHashesKnownToDelorean.keys
        var modifiedFiles: List[Path] = List.empty
        var deletedFiles: List[Path] = List.empty
        allFiles.foreach(path ⇒ {
            if (Files.exists(path)) {
                val hash = hasher.computeFileHash(path.toString, justGetTheHash = true)
                if (!allFilesAndHashesKnownToDelorean.exists(_ == (path, hash))) modifiedFiles = path :: modifiedFiles
            } else
                deletedFiles = path :: deletedFiles

        })
        (modifiedFiles.map(_.toString), deletedFiles.map(_.toString))
    }

    def getUntrackedFiles: Set[String] = {
        val allFilesDeloreanKnows: Set[Path] = allFilesAndHashesKnownToDelorean.keys.toSet

        // ".tm" directory should be ignored always
        val biffFileContents: Set[String] = Set(".tm") ++ {
            if (new File(IGNORE_FILE).exists()) getLinesOfFile(IGNORE_FILE).toSet else Set.empty[String]
        }

        logger.fine(s"biffFileContents : $biffFileContents")

        var ignoredFiles: Set[Path] = Set.empty
        biffFileContents.foreach {
            path ⇒ {
                if (Files.isDirectory(Paths.get(path))) {
                    ignoredFiles ++= getFilesRecursively(path).map(Paths.get(_)).toSet
                }
                else {
                    ignoredFiles += Paths.get(path)
                }
            }
        }
        logger.fine(s"Ignored files: $ignoredFiles")

        //        val predicate: Predicate[Path] = {
        //            p: Path ⇒ !allFilesDeloreanKnows.contains(p) && !ignoredFiles.contains(p)
        //        }
        val allFilesInMainDirectory: Set[Path] = getFilesRecursively(".").map(x ⇒ Paths.get(x)).toSet
        val untrackedFiles: Set[Path] = allFilesInMainDirectory -- allFilesDeloreanKnows -- ignoredFiles
        logger.fine(s"Untracked files: $untrackedFiles")
        untrackedFiles.map(_.toString)
    }

}