/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean

import org.junit.Assert._
import org.junit.Test

class ConfigurationTest {
    @Test
    def whenConfigurationNotPresent(): Unit = {
        assertEquals("", Configuration("alakazam"))
        assertEquals("", Configuration("Erno"))
    }
}
