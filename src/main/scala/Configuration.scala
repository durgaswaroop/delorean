import java.io.File

/**
  * Has a Map with the Configuration information from config file.
  */
object Configuration {
    val configFile = ".tm/config"
    val configurationMap: Map[String, String] = {
        if (new File(configFile).exists()) FileOps.getFileAsMap(configFile) else Map.empty
    }

    def apply(key: String): String = configurationMap.getOrElse(key, "")
}
