/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean
package commands

/**
  * Class for the command 'pitstop'.
  */
case class Pitstop(pitstopArgs: List[String]) {
  // Taking the second argument because the first argument will be "-rl"
  Hasher.computePitStopHash(pitstopArgs(1))
}
