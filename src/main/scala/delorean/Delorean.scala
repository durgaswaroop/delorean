package delorean

import java.util.logging.Logger

/**
  * Delorean main class.
  */
object Delorean {
    val logger: Logger = Logger.getLogger(this.getClass.getName)


    def main(args: Array[String]): Unit = {
        logger.fine(s"Length of Arguments = ${args.length}")

        // Call to configuration singleton to prepare the configuration map
        Configuration

        // Reconstruct file "67497b776854008d38c2340e14925a64b36686230bccaa777db68f644196015f"

        if (args.length == 0) Usage("full")
        else ParseOption(args.toList)
    }
}
