package delorean.commands

import java.io.File

import delorean.CONFIG
import delorean.FileOps._

import scala.collection.mutable

/**
  * Class for 'config' command.
  */
case class Config(configArgs: List[String]) {
    if (configArgs.length == 1) {
        val configMap = getFileAsMap(CONFIG)
        configMap.foreach(kv ⇒ println(s"${kv._1} = ${kv._2}"))
    } else {
        writeMapToFile(mutable.LinkedHashMap(configArgs.head → configArgs(1)), "null", new File(CONFIG))
    }
}
