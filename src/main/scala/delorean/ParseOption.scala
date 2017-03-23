package delorean

import java.nio.file.{Files, Paths}

import delorean.commands._

/**
  * Parser for the command line options
  */
object ParseOption {

    val hasher: Hasher = new Hasher

    def apply(argsList: List[String]): Unit = argsList.head match {
        case "--help" ⇒ Usage("full")
        case "add" ⇒ add(argsList.tail)
        case "config" ⇒ config(argsList.tail)
        case "describe" ⇒ describe(argsList.tail)
        case "pitstop" ⇒ pitstop(argsList.tail)
        case "ride" ⇒ ride(argsList.tail)
        case "show-timeline" ⇒ showTimeLine(argsList.tail)
        case "status" ⇒ status(argsList.tail)
        case "version" | "-v" | "-V" | "--version" ⇒ version(argsList.tail)
        case unknown ⇒
            var command = unknown
            if (unknown.startsWith("-")) {
                command = unknown.dropWhile(c ⇒ c == '-')
                println(s"delorean: '$command' is not a valid option. See 'delorean --help'")
            } else println(s"delorean: '$command' is not a valid command. See 'delorean --help'")
    }

    private def add(addArgs: List[String]): Unit = if (addArgs.isEmpty) Usage("add") else Add(addArgs)

    private def config(configArgs: List[String]): Unit = {
        if (configArgs.isEmpty) Usage("config")
        else if (configArgs.length == 1) {
            configArgs.head match {
                case "--list" | "list" | "-l" ⇒ Config(configArgs)
                case _ ⇒ Usage("config")
            }
        }
        else if (configArgs.length == 2) Config(configArgs)
        else Usage("config")
    }

    private def describe(describeArgs: List[String]): Unit = {
        if (describeArgs.isEmpty) Usage("describe")
        else Describe(describeArgs)
    }

    private def pitstop(pitstopArgs: List[String]): Unit = {
        if (pitstopArgs.isEmpty || pitstopArgs.length != 2 || pitstopArgs.head != "-rl") Usage("pitstop")
        else Pitstop(pitstopArgs)
    }

    private def ride(rideArgs: List[String]): Unit = if (rideArgs.nonEmpty) Usage("ride") else new Ride

    private def showTimeLine(showTimeLineArgs: List[String]): Unit = {
        if (showTimeLineArgs.size >= 2) Usage("show-timeline")
        else if (showTimeLineArgs.size == 1) {
            val head: String = showTimeLineArgs.head
            if (head == "-s" || head == "--short") new ShowTimeLine
            else if (head == "-l" || head == "--long") ShowTimeLine(OutputFormat.LONG)
            else Usage("show-timeline")
        }
        else new ShowTimeLine
    }

    private def status(statusArgs: List[String]): Unit = {
        if (statusArgs.length > 1) Usage("status")
        else if (statusArgs.length == 1) {
            if (Files.exists(Paths.get(statusArgs.head))) Status(statusArgs.head)
            else println(s"File '${statusArgs.head}' does not exist")
        }
        else Status() //brackets are needed. DONT REMOVE
    }

    private def version(versionArgs: List[String]): Unit = {
        if (versionArgs.nonEmpty) Usage("version") else println(s"delorean version ${Version.version}")
    }
}
