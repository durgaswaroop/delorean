/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean

import delorean.FileOps.{getFileAsMap, getLinesOfFile, writeToFile}

import scala.collection.mutable

/**
  * Class to reconstruct the files based on the hashes.
  */
object Reconstruct {

    /**
      * Reconstructs a file.
      *
      * Will take the file hash we want to reconstruct/reset and will look for that hash in the hashes directory.
      * Once the file is found, it will look at the hashes of individual lines in the file and
      * gets the corresponding lines from the String pool file
      *
      * @param fileHash hash of the file
      */
    def file(fileName: String, fileHash: String): Unit = {
        val lineHashesOfFile: List[String] = getLinesOfFile(s"$HASHES_FOLDER$fileHash")
        val stringPoolMap: mutable.LinkedHashMap[String, String] = getFileAsMap(STRING_POOL)
        var reconstructedLines: String = ""
        lineHashesOfFile.foreach(lineHash ⇒ reconstructedLines += stringPoolMap(lineHash) + "\n")

        // Store it in to the fileName given. Overwrite existing content
        writeToFile(fileName, reconstructedLines)
    }
}
