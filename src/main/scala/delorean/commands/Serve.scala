package delorean
package commands

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{FileAlreadyExistsException, Files, Path, Paths}
import java.util.logging.Logger
import javax.servlet.http.HttpServletResponse

import com.google.gson.Gson
import spark.Response
import spark.Spark._

import scala.util.{Failure, Success, Try}

class Serve(val repoName: String) {
  val logger: Logger = Logger.getLogger(this.getClass.getName)

  val repoPath: Path = Paths.get(".").resolve(repoName)

  // If the directory already exists, throw an exception
  if (repoPath.toFile.exists())
    throw new FileAlreadyExistsException(repoPath.toString)

  // Create the repo directory
  Files.createDirectories(repoPath)

  // Initialize an empty repository in that directory
  new Ride(repoPath.toString + File.separator)

  // Set location from where static files would be served
  staticFiles.location(repoPath.toString)
  logger.fine(s"Serving static files from: ${repoPath.toString}")

  // Set the port
  port(DELOREAN_SERVER_PORT)

  //Start the server
  init()
  logger.fine(s"Git Server started for repo $repoPath")

  // Setup the Routes
  get(s"/$repoName/string_pool", (req, res) => serveStringPool(res))

  get(s"/$repoName/travelogue", (req, res) => serveTravelogue(res))

  get(s"/$repoName/timelines/:timeline",
      (req, res) => serveTimelineHash(req.params(":timeline"), res))

  get(s"/$repoName/pitstops/:hash", (req, res) => servePitstop(req.params(":hash"), res))

  get(s"/$repoName/metadata/:hash", (req, res) => serveMetadata(req.params(":hash"), res))

  get(s"/$repoName/files/:hash", (req, res) => serveHashFile(req.params(":hash"), res))

  get(s"/$repoName/indicators", (req, res) => serveIndicators(res))

  get(s"/$repoName/pitstops", (req, res) => servePitstops(res))

  // Sends back the list of all available pitstops
  def servePitstops(res: Response): HttpServletResponse = {
    var hashesListJsonString: String = "["

    // Add all the hashes into a comma separated string
    hashesListJsonString += new File(repoPath.resolve(PITSTOPS_FOLDER).toString)
      .listFiles()
      .map(_.getName)
      .mkString(",")

    hashesListJsonString += "]"

    logger.fine(hashesListJsonString)

    getByteStreamResponse(res, hashesListJsonString.getBytes(StandardCharsets.UTF_8)) match {
      case Success(jsonResponse) => jsonResponse
      case Failure(exception) =>
        exception.printStackTrace()
        null
    }
  }

  // Sends back the list of all available indicators
  def serveIndicators(res: Response): HttpServletResponse = {
    val indicatorsAndHashes: Array[IndicatorAndHash] =
      new File(repoPath.resolve(INDICATORS_FOLDER).toString)
        .listFiles()
        .map(IndicatorAndHash(_))

    indicatorsAndHashes.foreach(ih => logger.fine(ih.indicator + ":" + ih.hash))

    val jsonString = new Gson().toJson(indicatorsAndHashes)
    logger.fine(jsonString)

    getByteStreamResponse(res, jsonString.getBytes(StandardCharsets.UTF_8)) match {
      case Success(jsonResponse) => jsonResponse
      case Failure(exception) =>
        exception.printStackTrace()
        null
    }
  }

  // Sends back the latest pitstop hash of the given timeline
  def serveHashFile(hash: String, res: Response): HttpServletResponse = {
    // If the hash if a binary file
    if (repoPath.resolve(BINARIES_FOLDER).resolve(hash).toFile.exists()) {
      res.header("binary", "true")
      getFileStreamResponse(res, repoPath.resolve(BINARIES_FOLDER).resolve(hash)) match {
        case Success(binary_file) => binary_file
        case Failure(exception) =>
          exception.printStackTrace()
          null
      }
    } else { // If it is not binary file
      res.header("binary", "false")
      getFileStreamResponse(res, repoPath.resolve(HASHES_FOLDER).resolve(hash)) match {
        case Success(normal_file) => normal_file
        case Failure(exception) =>
          exception.printStackTrace()
          null
      }
    }
  }

  // Sends back the latest pitstop hash of the given timeline
  def serveTimelineHash(timeline: String, res: Response): HttpServletResponse = {
    getFileStreamResponse(res, repoPath.resolve(INDICATORS_FOLDER + "/" + timeline)) match {
      case Success(current) => current
      case Failure(exception) =>
        exception.printStackTrace()
        null
    }
  }

  // Sends back the metadata file associated with the given pitstop hash
  def serveMetadata(pitstopHash: String, res: Response): HttpServletResponse = {
    getFileStreamResponse(res, repoPath.resolve(METADATA_FOLDER + "/" + pitstopHash)) match {
      case Success(metadata) => metadata
      case Failure(exception) =>
        exception.printStackTrace()
        null
    }
  }

  // Sends back the pitstop file associated with the given pitstop hash
  def servePitstop(pitstopHash: String, res: Response): HttpServletResponse = {
    getFileStreamResponse(res, repoPath.resolve(PITSTOPS_FOLDER + "/" + pitstopHash)) match {
      case Success(pitstop) => pitstop
      case Failure(exception) =>
        exception.printStackTrace()
        null
    }
  }

  // Sends back the travelogue file
  def serveTravelogue(res: Response): HttpServletResponse = {
    getFileStreamResponse(res, repoPath.resolve(TRAVELOGUE)) match {
      case Success(travelogue) => travelogue
      case Failure(exception) =>
        exception.printStackTrace()
        null
    }
  }

  // Sends the string_pool file
  def serveStringPool(res: Response): HttpServletResponse = {
    getFileStreamResponse(res, repoPath.resolve(STRING_POOL)) match {
      case Success(stringPool) => stringPool
      case Failure(exception) =>
        exception.printStackTrace()
        null
    }
  }

  // Reads the contents of the file and puts it in the response stream to be returned back to the client
  private def getFileStreamResponse(res: Response, filePath: Path): Try[HttpServletResponse] = {
    val fileBytes = Files.readAllBytes(filePath)
    getByteStreamResponse(res, fileBytes)
  }

  // Constructs the response stream to be returned back to the client from the bytes given
  private def getByteStreamResponse(res: Response, bytes: Array[Byte]): Try[HttpServletResponse] = {
    Try {
      val raw = res.raw()
      raw.getOutputStream.write(bytes)
      raw.getOutputStream.flush()
      raw.getOutputStream.close()
      logger.info("Raw response constructed")

      raw
    }
  }

}
