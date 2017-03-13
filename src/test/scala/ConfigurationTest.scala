import delorean.Configuration
import org.junit.gen5.api.Assertions._
import org.junit.gen5.api.Test

class ConfigurationTest {
    @Test
    def whenConfigurationNotPresent(): Unit = {
        assertEquals("", Configuration("alakazam"))
        assertEquals("", Configuration("Erno"))
    }
}
