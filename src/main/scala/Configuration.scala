object Configuration {
    val configFile = ".tm/config"
    val configurationMap: Map[String, String] = FileOps.getFileAsMap(configFile)

    def apply(key: String): String = configurationMap.getOrElse(key, "")
}
