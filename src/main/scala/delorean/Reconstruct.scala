/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean

import delorean.FileOps.{getFileAsMap, getLinesOfFile, writeToFile}

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
    def file(fileHash: String): Unit = {
        val lineHashesOfFile: List[String] = getLinesOfFile(s"$HASHES_FOLDER$fileHash")
        val stringPoolMap = getFileAsMap(STRING_POOL)
        var reconstructedLines: String = ""
        lineHashesOfFile.foreach(lineHash ⇒ reconstructedLines += stringPoolMap(lineHash) + "\n")
        // Store it in to the appropriate file from the file name in the travelogue file
        // Travelogue file gets updated with every pitstop. So, the hash for a particular hash might not even
        // exist in it. Instead we should get the filename from the pitstop to which we are trying to reset to.
        val fileName: String = getFileAsMap(TRAVELOGUE).filter(e ⇒ e._2 == fileHash).keys.head
        writeToFile(fileName, reconstructedLines)
    }
}
