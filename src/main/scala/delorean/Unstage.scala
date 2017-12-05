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
  val (realFiles: List[String], imaginaryFiles) = files.span(file => new File(file).exists())
  imaginaryFiles.foreach(file => println(s"delorean: File $file does not exist"))
  if (realFiles.isEmpty) System.exit(1)

  var filesToUnstage: List[String] = realFiles.map(filename => new File(filename).getAbsolutePath)
  logger.fine(s"Files to unstage: $filesToUnstage")

  val currentStagedFileMap: mutable.LinkedHashMap[String, String] = getFileAsMap(tempPitstopFile)
  var newStagedFileMap: mutable.LinkedHashMap[String, String] = currentStagedFileMap

  filesToUnstage foreach (newStagedFileMap -= _)
  logger.fine(s"New files after unstage: $newStagedFileMap")
  writeMapToFile(newStagedFileMap, tempPitstopFile)
}
