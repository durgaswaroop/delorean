import java.util.logging.Logger

object Delorean {
    val logger: Logger = Logger.getLogger(this.getClass.getName)

    def main(args: Array[String]): Unit = {
        val hasher: Hasher = new Hasher
        logger.fine(s"Length of Arguments = ${args.length}")
        if (args.length == 0) {
            println(
                """usage: delorean [--version] <command> [<args>]
                  |
                  | These are the common Delorean commands:
                  |
                  | ride        Tell Delorean to initialize a new repo
                  | add         Add files to repository
                  | pitstop     Record the changes to the repo
                """.stripMargin)
            System.exit(1)
        } else {
            args(0) match {
                // Will initialize a new repo
                case "ride" ⇒ new Ride
                case "pitstop" ⇒ hasher.computePitStopHash(args.tail)
                case _ ⇒ {
                    println("Invalid Option")
                    System.exit(1)
                }
            }
        }
    }
}