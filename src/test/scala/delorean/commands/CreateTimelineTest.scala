/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean.commands

import delorean.CURRENT_INDICATOR
import delorean.FileOps._
import org.junit.Assert._

class CreateTimelineTest {

    // @Test
    def createTimelineTest(): Unit = {
        // Saving the timeline that was there before the test is run
        val currentTimeline: String = getLinesOfFile(CURRENT_INDICATOR).head

        CreateTimeLine("master")
        assertEquals("master", getLinesOfFile(CURRENT_INDICATOR).head)

        writeToFile(CURRENT_INDICATOR, currentTimeline)
    }

}
