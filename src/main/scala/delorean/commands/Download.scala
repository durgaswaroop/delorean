package delorean
package commands

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.charset.StandardCharsets._
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
    fromURL(stringPoolUrl).mkString.getBytes(UTF_8)
  Files.write(Paths.get(reponame).resolve(STRING_POOL), stringPoolBytes)

  // Download travelogue
  val travelogueUrl: String = remoteRepoServerUrl + "travelogue"
  val travelogueBytes: Array[Byte] =
    fromURL(travelogueUrl).mkString.getBytes(UTF_8)
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
    Files.write(indicatorPath, ih.hash.getBytes(UTF_8))
  })

  // Download pitstops and associated metadata
  val pitstopsUrl: String = remoteRepoServerUrl + "pitstops"
  val metadataUrl: String = remoteRepoServerUrl + "metadata"
  val pitstopsJson: String = fromURL(pitstopsUrl).mkString
  val pitstops: Array[String] = new Gson().fromJson(pitstopsJson, classOf[Array[String]])
  pitstops.foreach(pitstop => {
    // For each pitstop hash, query the endpoints for pitstop content and the associated metadata
    val pitstopPath = Paths.get(reponame).resolve(PITSTOPS_FOLDER).resolve(pitstop)
    val metadataPath = Paths.get(reponame).resolve(METADATA_FOLDER).resolve(pitstop)
    pitstopPath.toFile.createNewFile()
    metadataPath.toFile.createNewFile()
    val pitstopBytes =
      fromURL(pitstopsUrl + "/" + pitstop).mkString.getBytes(UTF_8)
    val metadataBytes =
      fromURL(metadataUrl + "/" + pitstop).mkString.getBytes(UTF_8)

    Files.write(pitstopPath, pitstopBytes)
    Files.write(metadataPath, metadataBytes)
  })

  // Download file hashes of binary and normal files
  val fileHashesUrl: String = remoteRepoServerUrl + "file-hashes"
  val normalFilesUrl: String = remoteRepoServerUrl + "normal-files/"
  val binaryFilesUrl: String = remoteRepoServerUrl + "binary-files/"
  val fileHashesJson: String = fromURL(fileHashesUrl).mkString
  val fileHashes: FileHashesPOJO = new Gson().fromJson(fileHashesJson, classOf[FileHashesPOJO])
  // Download all the non-binary files
  fileHashes.normalFiles.foreach(hash => {
    val fileHashPath = Paths.get(reponame).resolve(HASHES_FOLDER).resolve(hash)
    val fileBytes = fromURL(normalFilesUrl + hash).mkString.getBytes(UTF_8)
    Files.write(fileHashPath, fileBytes)
  })
  // Download all the binary files
  fileHashes.binaryFiles foreach (hash => {
    val fileHashPath = Paths.get(reponame).resolve(BINARIES_FOLDER).resolve(hash)
    val fileBytes = fromURL(binaryFilesUrl + hash).mkString.getBytes(ISO_8859_1)
    Files.write(fileHashPath, fileBytes)
  })

  // Once everything is download, GoTo the latest commit on 'present' branch
  GoTo("present", reponame)

  def getRemoteRepoServerUrl(remoteRepo: String): (String, String) = {
    val pattern = "(https?://.*)/(.*)".r
    remoteRepo match {
      case pattern(hostname, repositoryName) =>
        val directoryName =
          if (repositoryName.matches(".*\\.delorean"))
            repositoryName.replace(".delorean", "") + File.separator
          else repositoryName + File.separator
        val repoServerUrl = hostname + ":" + DELOREAN_SERVER_PORT + "/" + repositoryName + "/"
        logger.fine(s"Repo server Url constructed: $repoServerUrl")
        (repoServerUrl, repositoryName + File.separator)
      case _ => throw new InvalidRepositoryUrlException()
    }
  }
}
