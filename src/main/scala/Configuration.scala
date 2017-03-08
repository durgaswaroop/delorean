import java.io.File

import scala.collection.mutable

/**
  * Has a Map with the Configuration information from config file.
  */
object Configuration {
    val configFile = ".tm/config"
    val configurationMap: mutable.LinkedHashMap[String, String] = {
        if (new File(configFile).exists()) FileOps.getFileAsMap(configFile) else mutable.LinkedHashMap.empty
    }

    def apply(key: String): String = configurationMap.getOrElse(key, "")
}
