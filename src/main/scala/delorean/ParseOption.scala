package delorean

import java.io.File
import java.nio.file.{Files, Paths}

import delorean.FileOps.writeMapToFile
import delorean.commands._

import scala.collection.mutable

/**
  * Parser for the command line options
  */
object ParseOption {

    val hasher: Hasher = new Hasher

    def apply(argsList: List[String]): Unit = argsList.head match {
        case "--help" ⇒ Usage("full")
        case "-v" | "-V" | "--version" ⇒ version(argsList.tail)
        case "ride" ⇒ ride(argsList.tail)
        case "add" ⇒ add(argsList.tail)
        case "pitstop" ⇒ pitstop(argsList.tail)
        case "config" ⇒ config(argsList.tail)
        case "status" ⇒ status(argsList.tail)
        case "show-timeline" ⇒ showTimeLine(argsList.tail)
        case unknown ⇒ {
            var command = unknown
            if (unknown.startsWith("-")) {
                command = unknown.dropWhile(c ⇒ c == '-')
                println(s"delorean: '$command' is not a valid option. See 'delorean --help'")
            } else println(s"delorean: '$command' is not a valid command. See 'delorean --help'")
        }
    }

    private def ride(rideArguments: List[String]): Unit = if (rideArguments.nonEmpty) Usage("ride") else new Ride

    private def add(addArguments: List[String]): Unit = if (addArguments.isEmpty) Usage("add") else {
        hasher.computeHashOfAddedFiles(addArguments.toArray)
    }

    private def pitstop(pitstopArguments: List[String]): Unit = {
        if (pitstopArguments.isEmpty || pitstopArguments.length != 2 || pitstopArguments.head != "-rl") Usage("pitstop")
        else hasher.computePitStopHash(pitstopArguments(1))
    }

    private def config(configArgs: List[String]): Unit = {
        if (configArgs.isEmpty || configArgs.length != 2) Usage("config")
        else writeMapToFile(mutable.LinkedHashMap(configArgs.head → configArgs(1)), "null", new File(CONFIG))
    }

    private def version(versionArguments: List[String]): Unit = {
        if (versionArguments.nonEmpty) Usage("version") else println(s"delorean version ${Version.version}")
    }

    private def status(statusArguments: List[String]): Unit = {
        if (statusArguments.length > 1) Usage("status")
        else if (statusArguments.length == 1) {
            if (Files.exists(Paths.get(statusArguments.head))) println("Status: Coming soon")
            else println(s"File '${statusArguments.head}' does not exist")
        }
        else new Status
    }

    private def showTimeLine(showTimeLineArguments: List[String]): Unit = {
        if (showTimeLineArguments.size >= 2) Usage("show-timeline")
        else if (showTimeLineArguments.size == 1) {
            val head: String = showTimeLineArguments.head
            if (head == "-s" || head == "--short") new ShowTimeLine
            else if (head == "-l" || head == "--long") new ShowTimeLine(OutputFormat.LONG)
            else Usage("show-timeline")
        }
        else new ShowTimeLine
    }
}
