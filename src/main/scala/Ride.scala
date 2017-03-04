import java.io.File

class Ride {
    val deloreanDirectory = new File(".tm/")
    val pitstopDirectory = new File(".tm/pitstops/")
    val hashesDirectory = new File(".tm/hashes/")

    pitstopDirectory.mkdirs
    hashesDirectory.mkdirs
    println("Delorean repository created. Delorean is ready for a ride")
}
