import delorean.Hasher
import org.junit.gen5.api.Assertions.{assertEquals, assertNotEquals}
import org.junit.gen5.api.{AfterEach, BeforeEach, Test}

class HasherTest {

    val testFile = "src/test/resources/test"
    val testCopyFile = "src/test/resources/test_copy"
    val testDiffFile = "src/test/resources/test_diff"
    val outputFile = "src/test/resources/out"
    val travelogueFile = "src/test/resources/.tm/travelogue"
    var hasher: Hasher = _

    @BeforeEach def setUp(): Unit = hasher = new Hasher

    @AfterEach def tearDown(): Unit = hasher = null

    @Test
    def computeHashTest(): Unit = {
        val string1 = "Hello"
        val string2 = "world"
        assertNotEquals(hasher.computeHash(string1, "SHA-256"), hasher.computeHash(string2, "SHA-256"))
        assertEquals(hasher.computeHash(string1, "SHA-256"), hasher.computeHash(string1, "SHA-256"))
    }

}
