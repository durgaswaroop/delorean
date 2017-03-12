import java.io.File

import Variables._

class Ride {
    val deloreanDirectory = new File(TIME_MACHINE)
    val pitstopDirectory = new File(PITSTOPS_FOLDER)
    val hashesDirectory = new File(HASHES_FOLDER)
    val metadataDirectory = new File(METADATA_FOLDER)
    val indicatorsDirectory = new File(INDICATORS_FOLDER)

    val configFile = new File(CONFIG)
    val currentTimeLine = new File(CURRENT_INDICATOR)

    pitstopDirectory.mkdirs
    hashesDirectory.mkdirs
    metadataDirectory.mkdirs
    indicatorsDirectory.mkdir
    currentTimeLine.createNewFile
    configFile.createNewFile
    println("Delorean repository created. Delorean is ready for a ride")
}
