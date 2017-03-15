import delorean.Configuration
import org.junit.Assert._
import org.junit.Test

class ConfigurationTest {
    @Test
    def whenConfigurationNotPresent(): Unit = {
        assertEquals("", Configuration("alakazam"))
        assertEquals("", Configuration("Erno"))
    }
}
