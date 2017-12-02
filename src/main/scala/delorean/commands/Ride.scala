/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean
package commands

import java.io.File

class Ride(directory: String = "") {
  if (isDeloreanRepo) {
    print("""delorean: Current directory is already part of a delorean repository.
        |Nothing else to be done.
            """.stripMargin)
    System.exit(0)
  }

  val pitstopDirectory = new File(directory + PITSTOPS_FOLDER)
  val hashesDirectory = new File(directory + HASHES_FOLDER)
  val metadataDirectory = new File(directory + METADATA_FOLDER)
  val indicatorsDirectory = new File(directory + INDICATORS_FOLDER)
  val binariesDirectory = new File(directory + BINARIES_FOLDER)

  val configFile = new File(directory + CONFIG)
  val stringPoolFile = new File(directory + STRING_POOL)
  val travelogueFile = new File(directory + TRAVELOGUE)
  val currentFile = new File(directory + CURRENT_INDICATOR)
  val defaultTimelineFile = new File(directory + DEFAULT_TIMELINE)

  pitstopDirectory.mkdirs
  hashesDirectory.mkdirs
  metadataDirectory.mkdirs
  indicatorsDirectory.mkdir
  binariesDirectory.mkdir
  configFile.createNewFile
  stringPoolFile.createNewFile
  travelogueFile.createNewFile
  currentFile.createNewFile
  defaultTimelineFile.createNewFile

  // Add "present" to the current indicator file as "present" is the default timeline
  FileOps.writeToFile(currentFile.getPath, "present")

  println("Delorean repository created. Delorean is ready for a ride")
}
