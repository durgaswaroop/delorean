/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean.commands

import java.io.{ByteArrayOutputStream, PrintStream}
import java.nio.file._

import delorean._
import org.junit.Assert._
import org.junit._

import scala.util.Try

class ConfigTest {

    val outContent = new ByteArrayOutputStream()

    /**
      * This will redirect the output stream into outContent and we can access the print output from it.
      */
    @Before
    def setUpStreams(): Unit = System.setOut(new PrintStream(outContent))

    /**
      * Resetting the stream to null.
      */
    @After
    def cleanUpStreams(): Unit = System.setOut(null)

    @Test
    def configTest(): Unit = {
        // Adding a new key value pair to configuration
        Config(List("Marty", "McFly"))

        // Calling list to display the current configuration
        Config(List("--list"))
        assertEquals("Marty = McFly", outContent.toString.trim)
    }
}

object ConfigTest {
    // Create CONFIG file. We also create TIME_MACHINE as we need the parent directory to be present
    @BeforeClass
    def setUp(): Unit = {
        Try(Files.createDirectory(Paths.get(TIME_MACHINE)))
        Try(Files.createFile(Paths.get(CONFIG)))
    }

    // Delete CONFIG file and the parent directory.
    @AfterClass
    def tearDown(): Unit = {
        Try(Files.delete(Paths.get(CONFIG)))
        Try(Files.delete(Paths.get(TIME_MACHINE)))
    }

}
