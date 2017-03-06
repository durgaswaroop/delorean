/**
  * Prints usage information for all the Delorean commands.
  */
object Usage {
    def apply(command: String): Unit = command match {
        case "full" ⇒ fullUsage()
        case "ride" ⇒ rideUsage()
        case "add" ⇒ addUsage()
        case "pitstop" ⇒ pitstopUsage()
        case _ ⇒ println(s"Usage information for the command $command is not available.")
    }

    def fullUsage(): Unit = {
        println(
            """usage: delorean [--version] <command> [<args>]
              |
              | These are the common Delorean commands:
              |
              | ride        Tell Delorean to initialize a new repo
              | add         Add files to repository
              | pitstop     Record the changes to the repo
            """.stripMargin)
    }

    def rideUsage(): Unit = {
        println(
            """
              |Usage: delorean ride
              |                - Initializes a new Delorean Repository
              |
              |For more: delorean --help
            """.stripMargin)
    }

    def addUsage(): Unit = {
        println(
            """
              |Usage: delorean add <file1> <file2> ..
              |
              |For more: delorean --help
            """.stripMargin)
    }

    def pitstopUsage(): Unit = {
        println(
            """
              |Usage: delorean pitstop -rl "<Rider Log>"
              |                - Creates a new pitstop with the given Rider log.
              |
              |For more: delorean --help
            """.stripMargin)
    }
}
