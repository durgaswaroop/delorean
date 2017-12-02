/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean

import delorean.Hasher.computeShaHash

import scala.collection._

/**
  * A single dictionary of all the files available for Delorean during the programs execution.
  * With this we won't access same file multiple times at different places.
  */
class FileDictionary() {

  // file -> DeloreanFile will be dictionary to check for every file
  // Delorean file will have lines list and hash string
  private val file_Lines_Hash_Map: mutable.Map[String, DeloreanFile] =
    mutable.Map.empty

}

object FileDictionary {
  var fileDictionary: FileDictionary = _

  /**
    * Returns a 'DeloreanFile' object when this is called. From that object we can extract the hash or lines or both as needed.
    *
    * @param file        : File for which the computation needs to be done
    * @param hashNeeded  : Set to true if hash of the file is needed
    * @param linesNeeded : Set to true if the list of lines is needed
    * @return : DeloreanFile object
    */
  def apply(file: String,
            hashNeeded: Boolean = false,
            linesNeeded: Boolean = false): DeloreanFile = {
    val deloreanFile: Option[DeloreanFile] =
      getInstance().file_Lines_Hash_Map.get(file)
    deloreanFile match {
      case Some(df) =>
        if (hashNeeded && linesNeeded && df.hash.nonEmpty && df.lines.nonEmpty)
          df
        else if (hashNeeded && df.hash.nonEmpty) df
        else if (linesNeeded && df.lines.nonEmpty) df
        else {
          val hash = if (hashNeeded) computeShaHash(file) else ""
          val lines =
            if (linesNeeded) FileOps.getLinesOfFile(file)
            else List.empty[String]
          getInstance().file_Lines_Hash_Map += (file -> DeloreanFile(lines, hash))
          DeloreanFile(lines, hash)
        }
      case None =>
        val hash = if (hashNeeded) computeShaHash(file) else ""
        val lines =
          if (linesNeeded) FileOps.getLinesOfFile(file) else List.empty[String]
        getInstance().file_Lines_Hash_Map += (file -> DeloreanFile(lines, hash))
        DeloreanFile(lines, hash)
    }
  }

  /**
    * Returns an instance of FileDictionary object.
    *
    * @return : Instance
    */
  def getInstance(): FileDictionary =
    if (fileDictionary == null) {
      fileDictionary = new FileDictionary()
      fileDictionary
    } else {
      fileDictionary
    }
}

/**
  * Simple case class to keep information regarding List of lines and the hash of a file.
  *
  * @param lines : List of lines of the file
  * @param hash  : SHA256 hash of the file
  */
case class DeloreanFile(lines: List[String] = List.empty, hash: String = "")
