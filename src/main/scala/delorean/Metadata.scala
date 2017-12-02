/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean

import java.nio.file.{Files, Paths}
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ofPattern

import delorean.FileOps.getLinesOfFile

/**
  * Class for holding metadata information. Basically a POJO.
  */
case class Metadata(pitstop: String,
                    time: ZonedDateTime,
                    rider: String,
                    parents: Array[String],
                    riderLog: String)

object Metadata {

  /**
    * Creates a Metadata object by parsing the metadata file of a pitstop
    * Typical metadata looks like this
    * *****
    * Time:Mar 12 2017 10:07 PM India Standard Time
    * Rider:dperla
    * Parent(s):abc123def456:qwe098rty765
    * RiderLog:
    * A new Rider in town
    * *****
    * And using that structure, we parse the file to get the required information
    *
    * @param pitstopHash : Pitstop for which we need Metadata
    * @return
    */
  def apply(pitstopHash: String): Metadata = {
    val metadataFile: String = {
      if (Files.exists(Paths.get(INDICATORS_FOLDER + pitstopHash)))
        METADATA_FOLDER + getLinesOfFile(INDICATORS_FOLDER + pitstopHash).head
      else
        METADATA_FOLDER + pitstopHash
    }
    val metadataFileContent: Seq[String] = getLinesOfFile(metadataFile)
    val time: ZonedDateTime = {
      ZonedDateTime.parse(
        metadataFileContent.head.split(":", 2)(1),
        ofPattern("MMM dd yyyy hh:mm a zzzz")
      )
    }
    val rider: String = metadataFileContent(1).split(":", 2)(1)
    // parents hashes will be separated by ':'. So, split will split across all those :'s and tail gives us
    // everything other than the first one.
    val parents: Array[String] = metadataFileContent(2).split(":").tail
    val riderLog: String = metadataFileContent.last
    new Metadata(pitstopHash, time, rider, parents, riderLog)
  }
}
