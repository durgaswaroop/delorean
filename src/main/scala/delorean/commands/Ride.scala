/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean
package commands

import java.io.File

class Ride {
    val deloreanDirectory = new File(TIME_MACHINE)

    if (deloreanDirectory.exists()) {
        print(
            """delorean: Current directory is already part of a delorean repository.
              |Nothing else to be done.
            """.stripMargin)
        System.exit(0)
    }

    val pitstopDirectory = new File(PITSTOPS_FOLDER)
    val hashesDirectory = new File(HASHES_FOLDER)
    val metadataDirectory = new File(METADATA_FOLDER)
    val indicatorsDirectory = new File(INDICATORS_FOLDER)
    val binariesDirectory = new File(BINARIES_FOLDER)

    val configFile = new File(CONFIG)
    val currentFile = new File(CURRENT_INDICATOR)
    val defaultTimelineFile = new File(DEFAULT_TIMELINE)

    pitstopDirectory.mkdirs
    hashesDirectory.mkdirs
    metadataDirectory.mkdirs
    indicatorsDirectory.mkdir
    binariesDirectory.mkdir
    configFile.createNewFile
    currentFile.createNewFile
    defaultTimelineFile.createNewFile

    // Add "present" to the current indicator file as "present" is the default timeline
    FileOps.writeToFile(currentFile.getPath, "present")

    println("Delorean repository created. Delorean is ready for a ride")
}
