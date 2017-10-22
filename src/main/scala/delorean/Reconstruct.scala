/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean

import java.nio.file.{CopyOption, Files, Paths, StandardCopyOption}
import java.util.logging.Logger

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
        val logger = Logger.getLogger(this.getClass.getName)
        logger.fine(s"filename: $fileName, fileHash: $fileHash")

        // TODO: DO a check for if the file is binary here
        val isBinaryFile = Files.exists(Paths.get(s"$BINARIES_FOLDER$fileHash"))

        // If the file is not a binary file its lines will be available in the String pool and using that we
        // can reconstruct the file
        if (!isBinaryFile) {
            val lineHashesOfFile: List[String] = getLinesOfFile(s"$HASHES_FOLDER$fileHash")
            logger.fine(s"Line hashes: $lineHashesOfFile")

            val stringPoolMap: mutable.LinkedHashMap[String, String] = getFileAsMap(STRING_POOL)

            var reconstructedLines: String = ""
            lineHashesOfFile.foreach(lineHash ⇒ reconstructedLines += stringPoolMap(lineHash) + "\n")
            logger.finest(reconstructedLines)

            // Store it in to the fileName given. Overwrite existing content
            writeToFile(fileName, reconstructedLines)
        } else { // If the file is binary file, then just copy that file to the destination
            val binaryFilePath = Paths.get(BINARIES_FOLDER + fileHash)
            val destinationFilePath = Paths.get(fileName)
            Files.copy(binaryFilePath, destinationFilePath, StandardCopyOption.REPLACE_EXISTING)
        }
    }
}
