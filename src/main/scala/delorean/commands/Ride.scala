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
    val currentPitstop = new File(CURRENT_INDICATOR)

    pitstopDirectory.mkdirs
    hashesDirectory.mkdirs
    metadataDirectory.mkdirs
    indicatorsDirectory.mkdir
    currentPitstop.createNewFile
    configFile.createNewFile
    println("Delorean repository created. Delorean is ready for a ride")
}

object Ride {
    def apply: Ride = new Ride()
}
