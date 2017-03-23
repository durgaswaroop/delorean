package delorean

import java.util.logging.{Level, Logger}


/**
  * Delorean main class.
  */
object Delorean {
    val logger: Logger = Logger.getLogger(this.getClass.getName)

    def main(args: Array[String]): Unit = {
        configureLogger()
        logger.fine(s"Length of Arguments = ${args.length}")

        // Call to configuration singleton to prepare the configuration map
        Configuration

        // Reconstruct file "67497b776854008d38c2340e14925a64b36686230bccaa777db68f644196015f"

        if (args.length == 0) Usage("full")
        else ParseOption(args.toList)
    }

    def configureLogger(): Unit = {
        val rootLogger: Logger = Logger.getLogger("")
        rootLogger.getHandlers.foreach(handler ⇒ handler.setLevel(Level.FINEST))
        System.getenv("DELOREAN_LOG_LEVEL") match {
            case "INFO" ⇒ rootLogger.setLevel(Level.INFO)
            case "FINE" ⇒ rootLogger.setLevel(Level.FINE)
            case _ ⇒ rootLogger.setLevel(Level.SEVERE)
        }
    }
}
