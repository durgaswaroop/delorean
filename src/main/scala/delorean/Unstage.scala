/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean

import java.io.File
import java.util.logging.Logger

import delorean.FileOps._

import scala.collection.mutable

case class Unstage(files: List[String]) {
  val logger: Logger = Logger.getLogger(this.getClass.getName)
  logger.fine(s"Unstaging list of files $files.")

  // If temp pitstop file is empty it means that no files are currently staged
  val tempPitstopFile: String = getTempPitstopFileLocation()
  if (tempPitstopFile.isEmpty) {
    println("""|delorean: No files staged
               |
               |For more: delorean --help
               |.""".stripMargin)
    System.exit(1)
  }

  // real files exist and imaginary files don't.
  val (realFiles, imaginaryFiles) = files.span(file => new File(file).exists())
  imaginaryFiles.foreach(file => println(s"delorean: File $file does not exist"))
  if (realFiles.isEmpty) System.exit(1)

  var filesToUnstage: List[String] = realFiles
  logger.fine(s"Files to unstage: $filesToUnstage")

  val currentStagedFileMap: mutable.LinkedHashMap[String, String] =
    getFileAsMap(tempPitstopFile)
  var newStagedFileMap: mutable.LinkedHashMap[String, String] =
    currentStagedFileMap
  logger.fine(s"New files after unstage: $newStagedFileMap")
  filesToUnstage foreach (newStagedFileMap -= _)
  writeMapToFile(newStagedFileMap, tempPitstopFile)
}
