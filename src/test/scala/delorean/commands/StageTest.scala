/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean.commands

import java.nio.file.{Files, Paths}

import delorean.FileOps._
import org.junit.Assert._
import org.junit.{AfterClass, Test}

class StageTest {
    @Test
    def stageTest(): Unit = {
        val filesToStage = List("src/test/resources/")
        Stage(filesToStage)
        val tempFile: String = getTempPitstopFile
        assertTrue("_temp file should have been created in Pitstops directory", tempFile.nonEmpty)
    }
}

object StageTest {
    @AfterClass def tearDown(): Unit = Files.delete(Paths.get(getTempPitstopFile))
}