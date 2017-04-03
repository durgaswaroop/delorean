/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean.commands

import java.nio.file.{Files, Paths}

import delorean.CURRENT_INDICATOR
import delorean.FileOps._
import org.junit.Assert._
import org.junit.{AfterClass, BeforeClass, Test}

class StageTest {
    @Test
    def stageTest(): Unit = {
        val filesToStage = List("src/test/resources/")
        Stage(filesToStage)
        val tempFile: String = getTempPitstopFileLocation
        assertTrue("_temp file should have been created in Pitstops directory", tempFile.nonEmpty)
    }
}

object StageTest {
    @BeforeClass
    def callToRide(): Unit = {
        // This will make sure it creates all the required files for the test. We are checking for CURRENT_INDICATOR
        // instead of TIME_MACHINE because .tm could be created by config test
        if (!Files.exists(Paths.get(CURRENT_INDICATOR))) new delorean.commands.Ride
    }

    @AfterClass def tearDown(): Unit = Files.delete(Paths.get(getTempPitstopFileLocation))
}