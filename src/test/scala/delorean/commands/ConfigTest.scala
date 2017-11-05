/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean.commands

import java.io.{ByteArrayOutputStream, File, PrintStream}
import java.nio.file._

import delorean._
import org.apache.commons.io.FileUtils
import org.junit.Assert._
import org.junit._

import scala.util.Try

class ConfigTest {

  @Test
  def configTest(): Unit = {
    // Adding a new key value pair to configuration
    Config(List("Marty", "McFly"))
    Config(List("Doc", "Brown"))
    Config(List("Time", "Machine"))

    val configLines: List[String] = FileOps.getLinesOfFile(CONFIG)

    assertEquals("Marty:McFly", configLines(0))
    assertEquals("Doc:Brown", configLines(1))
    assertEquals("Time:Machine", configLines(2))
  }
}

object ConfigTest {
  @BeforeClass
  def setUp(): Unit = {
    // This will make sure it creates all the required files for the test. We are checking for CURRENT_INDICATOR
    // instead of TIME_MACHINE because .tm could be created by config test
    if (!Files.exists(Paths.get(CURRENT_INDICATOR))) new delorean.commands.Ride
  }

  @AfterClass
  def tearDown(): Unit = {
    Try(FileUtils.deleteDirectory(new File(TIME_MACHINE)))
  }
}
