import com.typesafe.scalalogging.slf4j.LazyLogging

object Delorean extends LazyLogging {
    def main(args: Array[String]): Unit = {
        val hasher: Hasher = new Hasher
        logger.debug(s"Length of Arguments = ${args.length}")
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
            // args.foreach(hasher.computeHashOfFile)
            hasher.computePitStopHash(args)
        }
    }
}