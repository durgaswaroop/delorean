/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean.commands

import java.io.{ByteArrayOutputStream, PrintStream}

import org.junit.Assert._
import org.junit.{After, Before, Test}

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
    def configTest: Unit = {
        val TIMEMACHINE = "test/resources"

        // Adding a new key value pair to configuration
        Config(List("Marty", "McFly"))

        // Calling list to display the current configuration
        Config(List("--list"))
        assertEquals("Marty = McFly", outContent.toString.trim)
    }
}
