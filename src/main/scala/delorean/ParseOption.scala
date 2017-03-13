package delorean

import java.io.File

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
        case unknown ⇒ println(s"delorean: '$unknown' is not a valid delorean command. See 'delorean --help'")
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
        if (statusArguments.length > 1) Usage("status") else new Status
    }

    private def showTimeLine(showTimeLineArguments: List[String]): Unit = {
        if (showTimeLineArguments.nonEmpty) Usage("show-timeline") else new ShowTimeLine
    }
}
