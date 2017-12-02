/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean
package commands

import java.io.File
import java.util.logging.Logger

import delorean.FileOps._

/**
  * For the command 'stage'.
  */
case class Stage(files: List[String]) {
  val logger: Logger = Logger.getLogger(this.getClass.getName)
  logger.fine(s"Staging list of files $files.")

  // realFiles exist and imaginaryFiles don't
  val (realFiles, imaginaryFiles) =
    files.partition(file => new File(file).exists())

  imaginaryFiles.foreach(file => println(s"delorean: File $file does not exist"))

  if (realFiles.isEmpty) System.exit(1)
  logger.fine(s"Real files = $realFiles")

  var filesToStage: List[String] = Nil

  // If a directory is staged, get all the files of that directory
  realFiles.foreach(f =>
    if (new File(f).isDirectory) {
      logger.fine(s"In if for file $f")
      val directoryFiles: List[String] = getFilesRecursively(f)
      filesToStage = filesToStage ++ directoryFiles
    } else {
      logger.fine(s"File $f is not a directory. Entered else.")
      filesToStage ::= f
  })

  // In the list only keep the names of files and remove the directories.
  filesToStage = filesToStage.filterNot(file => new File(file).isDirectory)
  logger.fine(
    s"After resolving all the files to be staged (after deleting the directories) are: $filesToStage"
  )
  Hasher.computeHashOfStagedFiles(filesToStage)
}
