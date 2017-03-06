import java.io.File

/**
  * Parser for the command line options
  */
object ParseOption {

    val commands = List("ride", "add", "pitstop")

    val hasher: Hasher = new Hasher

    def apply(argsList: List[String]): Unit = argsList.head match {
        case "--help" ⇒ Usage("full")
        case "ride" ⇒ ride(argsList.tail)
        case "add" ⇒ add(argsList.tail)
        case "pitstop" ⇒ pitstop(argsList.tail)
        case "config" ⇒ config(argsList.tail)
        case unknown ⇒ println(s"Invalid Option: '$unknown'")
    }

    private def ride(rideArguments: List[String]): Unit = if (rideArguments.nonEmpty) Usage("ride") else new Ride

    private def add(addArguments: List[String]): Unit = if (addArguments.isEmpty) Usage("add") else {
        hasher.computeHashOfAddedFiles(addArguments.toArray)
    }

    private def pitstop(pitstopArguments: List[String]): Unit = if (pitstopArguments.isEmpty) Usage("pitstop") else {
        if (pitstopArguments.length != 2 || pitstopArguments.head != "-rl") Usage("pitstop")
        else hasher.computePitStopHash(pitstopArguments(1))
    }

    private def config(configArguments: List[String]): Unit = {
        if (configArguments.isEmpty || configArguments.length != 2) Usage("config")
        else FileOps.writeMapToFile(Map(configArguments(0) → configArguments(1)), "null", new File(".tm/config"))
    }

}