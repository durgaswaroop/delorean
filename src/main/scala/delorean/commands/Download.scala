package delorean
package commands

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.logging.Logger

import com.google.gson.Gson
import delorean.exceptions.InvalidRepositoryUrlException

import scala.io.Source.fromURL

/**
  * Class for 'download' command.
  */
case class Download(remoteRepo: String) {
  val logger: Logger = Logger.getLogger(this.getClass.getName)

  val (remoteRepoServerUrl, reponame) = getRemoteRepoServerUrl(remoteRepo)

  // Create the directory first
  Files.createDirectory(Paths.get(reponame))
  println(s"Downloading remote repository into directory $remoteRepo")

  // Start by initializing a normal repo
  new Ride(reponame)

  // Download string_pool
  val stringPoolUrl: String = remoteRepoServerUrl + "string_pool"
  val stringPoolBytes: Array[Byte] =
    fromURL(stringPoolUrl).mkString.getBytes(StandardCharsets.UTF_8)
  Files.write(Paths.get(reponame).resolve(STRING_POOL), stringPoolBytes)

  // Download travelogue
  val travelogueUrl: String = remoteRepoServerUrl + "travelogue"
  val travelogueBytes: Array[Byte] =
    fromURL(travelogueUrl).mkString.getBytes(StandardCharsets.UTF_8)
  Files.write(Paths.get(reponame).resolve(TRAVELOGUE), travelogueBytes)

  // Download indicators
  val indicatorsUrl: String = remoteRepoServerUrl + "indicators"
  val indicatorsJson: String = fromURL(indicatorsUrl).mkString
  val indicatorsAndHashes: Array[IndicatorAndHash] =
    new Gson().fromJson(indicatorsJson, classOf[Array[IndicatorAndHash]])
  indicatorsAndHashes.foreach(ih => {
    val indicatorPath = Paths.get(reponame).resolve(INDICATORS_FOLDER).resolve(ih.indicator)
    if (Files.notExists(indicatorPath)) {
      Files.createFile(indicatorPath)
    }
    // Write the hash to the file
    Files.write(indicatorPath, ih.hash.getBytes(StandardCharsets.UTF_8))
  })

  // Download pitstops
  val pitstopsUrl: String = remoteRepoServerUrl + "pitstops"
  val pitstopsJson: String = fromURL(pitstopsUrl).mkString
  val pitstops: Array[String] = new Gson().fromJson(pitstopsJson, classOf[Array[String]])
  pitstops.foreach(pitstop => {
    // For each pitstop hash query the endpoint for that pitstop content
    val pitstopPath = Paths.get(reponame).resolve(PITSTOPS_FOLDER).resolve(pitstop)
    pitstopPath.toFile.createNewFile()
    val pitstopBytes =
      fromURL(pitstopsUrl + "/" + pitstop).mkString.getBytes(StandardCharsets.UTF_8)
    Files.write(pitstopPath, pitstopBytes)
  })

  def getRemoteRepoServerUrl(remoteRepo: String): (String, String) = {
    val pattern = "(https?://.*)/(.*)".r
    remoteRepo match {
      case pattern(hostname, repositoryName) =>
        val directoryName =
          if (repositoryName.matches(".*\\.delorean")) repositoryName.replace(".delorean", "")
          else repositoryName
        val repoServerUrl = hostname + ":" + DELOREAN_SERVER_PORT + "/" + repositoryName + "/"
        logger.fine(s"Repo server Url constructed: $repoServerUrl")
        (repoServerUrl, repositoryName + File.separator)
      case _ => throw new InvalidRepositoryUrlException()
    }
  }
}
