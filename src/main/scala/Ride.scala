import java.io.File

class Ride {
    val deloreanDirectory = new File(".tm/")
    val pitstopDirectory = new File(".tm/pitstops/")
    val hashesDirectory = new File(".tm/hashes/")
    val metadataDirectory = new File(".tm/metadata/")
    val indicatorsDirectory = new File(".tm/indicators/")

    val configFile = new File(".tm/config")
    val currentTimeLine = new File(".tm/indicators/current")

    pitstopDirectory.mkdirs
    hashesDirectory.mkdirs
    metadataDirectory.mkdirs
    indicatorsDirectory.mkdir
    currentTimeLine.createNewFile
    configFile.createNewFile
    println("Delorean repository created. Delorean is ready for a ride")
}
