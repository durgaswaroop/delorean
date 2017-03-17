package delorean.commands

import java.io.File

import delorean.CONFIG
import delorean.FileOps.writeMapToFile

import scala.collection.mutable

/**
  * Class for 'config' command.
  */
case class Config(configArgs: List[String]) {
    writeMapToFile(mutable.LinkedHashMap(configArgs.head → configArgs(1)), "null", new File(CONFIG))
}
