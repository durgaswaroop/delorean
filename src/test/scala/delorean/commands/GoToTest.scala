package delorean.commands

import java.io.File
import java.nio.file.{Files, Paths}

import delorean.FileOps.{getLinesOfFile, getTempPitstopFileLocation}
import delorean.{CURRENT_INDICATOR, TIME_MACHINE}
import org.apache.commons.io.FileUtils
import org.junit.{AfterClass, BeforeClass, Test}
import org.junit.Assert._

import scala.util.Try

class GoToTest {

  @Test
  def goToTest(): Unit = {
    // Going to an invalid timeline
    GoTo("future")
    assertEquals(
      "Current timeline should be present",
      "present",
      getLinesOfFile(CURRENT_INDICATOR).head
    )

    // Going to an existing timeline
    CreateTimeLine("past")
    assertEquals("Current timeline should be past", "past", getLinesOfFile(CURRENT_INDICATOR).head)
  }
}

object GoToTest {
  @BeforeClass
  def callToRide(): Unit = {
    // This will make sure it creates all the required files for the test. We are checking for CURRENT_INDICATOR
    // instead of TIME_MACHINE because .tm could be created by config test
    if (!Files.exists(Paths.get(CURRENT_INDICATOR))) new delorean.commands.Ride
  }

  @AfterClass
  def tearDown(): Unit = {
    println("Tearing Down '.tm/' directory created for StageTest")
    Try(FileUtils.deleteDirectory(new File(TIME_MACHINE)))
  }
}
