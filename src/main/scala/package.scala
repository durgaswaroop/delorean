import java.io.File

import delorean.FileOps.filesMatchingInDir

/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

/**
  * Package object to hold variables.
  */
package object delorean {
    val TIME_MACHINE: String = ".tm/"

    val INDICATORS_FOLDER: String = TIME_MACHINE + "indicators/"
    val PITSTOPS_FOLDER: String = TIME_MACHINE + "pitstops/"
    val HASHES_FOLDER: String = TIME_MACHINE + "hashes/"
    val METADATA_FOLDER: String = TIME_MACHINE + "metadata/"
    val BINARIES_FOLDER: String = TIME_MACHINE + "bins/"

    val CONFIG: String = TIME_MACHINE + "config"
    val STRING_POOL: String = TIME_MACHINE + "string_pool"
    val TRAVELOGUE: String = TIME_MACHINE + "travelogue"
    val CURRENT_INDICATOR: String = INDICATORS_FOLDER + "current"
    val DEFAULT_TIMELINE: String = INDICATORS_FOLDER + "present"

    val IGNORE_FILE: String = ".biff"

    /**
      * Gets the full pitstop hash from the first few characters given
      *
      * @param simplifiedPitstop : pitstop to resolve
      * @return : Full pitstop hash if its present or else an empty string
      */
    def resolveTheCorrectPitstop(simplifiedPitstop: String): String = {
        val files: Array[File] = filesMatchingInDir(new File(PITSTOPS_FOLDER), _ startsWith simplifiedPitstop)
        if (files.length > 1) {
            println(
                s"""Ambiguous pitstop hash $simplifiedPitstop
                   |Found multiple pitstops matching this hash.
                """.stripMargin)
            "" // return empty string if more than one hash is found starting with the given characters
        } else if (files.length == 0) {
            println(s"Pitstop $simplifiedPitstop not found in the current repository")
            "" // return empty string if no hashes are found
        }
        else files.head.getName
    }
}
