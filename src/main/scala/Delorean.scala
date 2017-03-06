import java.util.logging.Logger

object Delorean {
    val logger: Logger = Logger.getLogger(this.getClass.getName)

    def main(args: Array[String]): Unit = {
        logger.fine(s"Length of Arguments = ${args.length}")

        // Call to configuration singleton to prepare the configuration map
        Configuration

        if (args.length == 0) Usage("full")
        else ParseOption(args.toList)
    }
}