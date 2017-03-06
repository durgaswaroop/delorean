import java.io.File

class Ride {
    val deloreanDirectory = new File(".tm/")
    val pitstopDirectory = new File(".tm/pitstops/")
    val hashesDirectory = new File(".tm/hashes/")
    val currentTimeLine = new File(".tm/pitstops/current")
    val configFile = new File(".tm/config")

    pitstopDirectory.mkdirs
    hashesDirectory.mkdirs
    currentTimeLine.createNewFile
    configFile.createNewFile
    println("Delorean repository created. Delorean is ready for a ride")
}
