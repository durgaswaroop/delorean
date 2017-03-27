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
}
