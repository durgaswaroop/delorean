import java.util.logging.Logger

object Delorean {
    val logger: Logger = Logger.getLogger(this.getClass.getName)

    def main(args: Array[String]): Unit = {
        logger.fine(s"Length of Arguments = ${args.length}")
        if (args.length == 0) Usage("full")
        else ParseOption(args.toList)
    }
}