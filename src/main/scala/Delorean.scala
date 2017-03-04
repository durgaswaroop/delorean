import java.util.logging.{Level, Logger}

object Delorean {
    val logger: Logger = Logger.getLogger(this.getClass.getName)

    def main(args: Array[String]): Unit = {
        val hasher: Hasher = new Hasher
        logger.log(Level.INFO, s"Length of Arguments = ${args.length}")
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
        } else {
            // Will initialize a new repo
            if (args(0) == "ride") new Ride
            else hasher.computePitStopHash(args)
        }
    }
}