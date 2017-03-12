import java.io.File

import Variables._

import scala.collection.mutable

/**
  * Has a Map with the Configuration information from config file.
  */
object Configuration {
    val configurationMap: mutable.LinkedHashMap[String, String] = {
        if (new File(CONFIG).exists()) FileOps.getFileAsMap(CONFIG) else mutable.LinkedHashMap.empty
    }

    def apply(key: String): String = configurationMap.getOrElse(key, "")
}
