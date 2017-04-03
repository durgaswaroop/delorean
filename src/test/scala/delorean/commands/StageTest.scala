/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean.commands

import java.io.File
import java.nio.file.{Files, Paths}

import delorean.FileOps._
import delorean._
import org.apache.commons.io.FileUtils
import org.junit.Assert._
import org.junit.{AfterClass, BeforeClass, Test}

import scala.util.Try

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

    @AfterClass def tearDown(): Unit = {
        Try(FileUtils.deleteDirectory(new File(TIME_MACHINE)))
        Try(Files.delete(Paths.get(getTempPitstopFileLocation)))
    }
}