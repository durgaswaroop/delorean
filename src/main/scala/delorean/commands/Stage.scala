/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean.commands

import java.io.File
import java.util.logging.Logger

import delorean.FileOps._
import delorean.Hasher

/**
  * For the command 'stage'.
  */
case class Stage(files: List[String]) {
    val logger: Logger = Logger.getLogger(this.getClass.getName)
    logger.fine(s"Staging list of files $files.")
    val hasher = new Hasher

    val (realFiles, imaginaryFiles) = files.span(file ⇒ new File(file).exists())

    imaginaryFiles.foreach(file ⇒ println(s"delorean: File $file does not exist"))
    if (realFiles.isEmpty) System.exit(1)

    var filesToStage: List[String] = realFiles

    // If a directory is staged, get all the files of the directory
    files.foreach(f ⇒
        if (new File(f).isDirectory) filesToStage = filesToStage ::: getFilesRecursively(f) else filesToStage = f :: filesToStage
    )
    // In the list only keep the names of the files.So
    filesToStage = filesToStage.filterNot(file ⇒ new File(file).isDirectory)
    logger.fine(s"After resolving all the files to be staged (after deleting the directories) are: $filesToStage")
    hasher.computeHashOfStagedFiles(filesToStage)
}
