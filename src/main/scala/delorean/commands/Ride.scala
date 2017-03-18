package delorean
package commands

import java.io.File

class Ride {
    val deloreanDirectory = new File(TIME_MACHINE)
    val pitstopDirectory = new File(PITSTOPS_FOLDER)
    val hashesDirectory = new File(HASHES_FOLDER)
    val metadataDirectory = new File(METADATA_FOLDER)
    val indicatorsDirectory = new File(INDICATORS_FOLDER)

    val configFile = new File(CONFIG)
    val currentFile = new File(CURRENT_INDICATOR)
    val defaultTimelineFile = new File(DEFAULT_TIMELINE)

    pitstopDirectory.mkdirs
    hashesDirectory.mkdirs
    metadataDirectory.mkdirs
    indicatorsDirectory.mkdir
    configFile.createNewFile
    currentFile.createNewFile
    defaultTimelineFile.createNewFile

    // Add "present" to the current indicator file as "present" is the default timeline
    FileOps.writeToFile(currentFile.getPath, "present")

    println("Delorean repository created. Delorean is ready for a ride")
}
