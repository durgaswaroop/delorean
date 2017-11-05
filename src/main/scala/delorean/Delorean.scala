/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean

import java.nio.file._
import java.util.logging.{Level, Logger}

import delorean.FileOps.{getCurrentPitstop, getTempPitstopFileLocation}

object Delorean {
    val logger: Logger = Logger.getLogger(this.getClass.getName)

    def main(args: Array[String]): Unit = {
        val start: Long = System.currentTimeMillis()
        configureLogger()
        logger.fine(s"Length of Arguments = ${args.length}")

        // Call to configuration singleton to prepare the configuration map
        Configuration

        if (isDeloreanRepo) deleteTempFileIfNotNeeded()

        if (args.length == 0) Usage("full")
        else ParseOption(args.toList)
        val end: Long = System.currentTimeMillis()
        val timeTaken = end - start
        println(s"Time taken: $timeTaken ms")
    }

    // Because of file stream not getting closed, the temp file which should have been deleted ideally is not getting deleted.
    // This will make sure we will delete it based on the modified Times
    def deleteTempFileIfNotNeeded(): Unit = {
        val lastPitstop = PITSTOPS_FOLDER + getCurrentPitstop
        val tempFile = getTempPitstopFileLocation

        if (lastPitstop.nonEmpty && tempFile.nonEmpty) {
            val lastPitstopTime = Files.getLastModifiedTime(Paths.get(lastPitstop))
            val tempFileTime = Files.getLastModifiedTime(Paths.get(tempFile))

            lastPitstopTime compareTo tempFileTime match {
                case 1 ⇒ Files.delete(Paths.get(getTempPitstopFileLocation))
                case _ ⇒
            }
        }
    }

    def configureLogger(): Unit = {
        val rootLogger: Logger = Logger.getLogger("")
        rootLogger.getHandlers.foreach(handler ⇒ handler.setLevel(Level.FINEST))
        System.getenv("DELOREAN_LOG_LEVEL") match {
            case "INFO" ⇒ rootLogger.setLevel(Level.INFO)
            case "FINE" ⇒ rootLogger.setLevel(Level.FINE)
            case _ ⇒ rootLogger.setLevel(Level.SEVERE)
        }
    }

}
