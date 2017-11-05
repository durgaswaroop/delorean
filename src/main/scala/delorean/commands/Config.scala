/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean
package commands

import java.util.logging.Logger

import delorean.CONFIG
import delorean.FileOps._

import scala.collection.mutable

/**
  * Class for 'config' command.
  */
case class Config(configArgs: List[String]) {
  val logger: Logger = Logger.getLogger(this.getClass.getName)
  logger.fine(s"Configuration called with args: $configArgs")

  if (configArgs.length == 1) { // Only when the argument is "--list" or "list"
    val configMap = getFileAsMap(CONFIG)
    configMap.foreach(kv => println(s"${kv._1} = ${kv._2}"))
  } else { // When a key value pair is provided
    writeMapToFile(mutable.LinkedHashMap(configArgs.head -> configArgs(1)),
                   CONFIG,
                   append = true)
  }
}
