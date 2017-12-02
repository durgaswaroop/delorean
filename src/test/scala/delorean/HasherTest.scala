/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean

import org.junit.Assert._
import org.junit._

class HasherTest {

  val testFile = "src/test/resources/test"
  val testCopyFile = "src/test/resources/test_copy"
  val testDiffFile = "src/test/resources/test_diff"
  val outputFile = "src/test/resources/out"
  val travelogueFile = "src/test/resources/.tm/travelogue"

  @Test
  def computeHashTest(): Unit = {
    val string1 = "Hello"
    val string2 = "world"
    assertNotEquals(
      Hasher.computeStringHash(string1, "SHA-256"),
      Hasher.computeStringHash(string2, "SHA-256")
    )
    assertEquals(
      Hasher.computeStringHash(string1, "SHA-256"),
      Hasher.computeStringHash(string1, "SHA-256")
    )
  }

}
