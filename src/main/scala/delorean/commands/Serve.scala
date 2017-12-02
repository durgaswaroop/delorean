package delorean
package commands

import java.io.File
import java.nio.file.{FileAlreadyExistsException, Files, Path, Paths}
import java.util.logging.Logger
import javax.servlet.http.HttpServletResponse

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

  //Start the server
  init()
  logger.fine(s"Git Server started for repo $repoPath")

  // Setup the Routes
  get(s"/$repoName/string_pool", (req, res) => serveStringPool(res))

  get(s"/$repoName/travelogue", (req, res) => serveTravelogue(res))

  get(s"/$repoName/:timeline",
      (req, res) => serveTimelineHash(req.params(":timeline"), res))

  get(s"/$repoName/pitstop/:hash",
      (req, res) => servePitstop(req.params(":hash"), res))

  get(s"/$repoName/metadata/:hash",
      (req, res) => serveMetadata(req.params(":hash"), res))

  // Sends back the latest pitstop hash of the given timeline
  def serveTimelineHash(timeline: String,
                        res: Response): HttpServletResponse = {
    getFileStreamResponse(
      res,
      repoPath.resolve(INDICATORS_FOLDER + "/" + timeline)) match {
      case Success(current) => current
      case Failure(exception) =>
        exception.printStackTrace()
        null
    }
  }

  // Sends back the metadata file associated with the given pitstop hash
  def serveMetadata(pitstopHash: String, res: Response): HttpServletResponse = {
    getFileStreamResponse(
      res,
      repoPath.resolve(METADATA_FOLDER + "/" + pitstopHash)) match {
      case Success(metadata) => metadata
      case Failure(exception) =>
        exception.printStackTrace()
        null
    }
  }

  // Sends back the pitstop file associated with the given pitstop hash
  def servePitstop(pitstopHash: String, res: Response): HttpServletResponse = {
    getFileStreamResponse(
      res,
      repoPath.resolve(PITSTOPS_FOLDER + "/" + pitstopHash)) match {
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
  private def getFileStreamResponse(
      res: Response,
      filePath: Path): Try[HttpServletResponse] = {
    Try {
      val raw = res.raw()
      raw.getOutputStream.write(Files.readAllBytes(filePath))
      raw.getOutputStream.flush()
      raw.getOutputStream.close()
      logger.info("Raw response constructed")

      raw
    }
  }
}
