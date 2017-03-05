/**
  * Parser for the command line options
  */
object ParseOption {

    val commands = List("ride", "add", "pitstop")

    def apply(argsList: List[String]): Unit = argsList.head match {
        case "--help" ⇒ Usage("full")
        case "ride" ⇒ ride(argsList.tail)
        case "add" ⇒ add(argsList.tail)
        case "pitstop" ⇒ pitstop(argsList.tail)
        case unknown ⇒ println(s"Invalid Option: '$unknown'")
    }

    private def ride(rideArguments: List[String]): Unit = if (rideArguments.nonEmpty) Usage("ride") else new Ride

    private def add(addArguments: List[String]): Unit = if (addArguments.isEmpty) Usage("add") else ???

    private def pitstop(pitstopArguments: List[String]): Unit = if (pitstopArguments.nonEmpty) Usage("pitstop") else ???

}