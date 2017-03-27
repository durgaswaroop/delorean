/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean.commands

import delorean.Hasher

/**
  * Class for the command 'pitstop'.
  */
case class Pitstop(pitstopArgs: List[String]) {
    val hasher = new Hasher
    // Taking the second argument because the first argument will be "-rl"
    hasher.computePitStopHash(pitstopArgs(1))
}
