package delorean

import org.junit.Assert._
import org.junit._

class HasherTest {

    val testFile = "src/test/resources/test"
    val testCopyFile = "src/test/resources/test_copy"
    val testDiffFile = "src/test/resources/test_diff"
    val outputFile = "src/test/resources/out"
    val travelogueFile = "src/test/resources/.tm/travelogue"
    var hasher: Hasher = _

    @Before def setUp(): Unit = hasher = new Hasher

    @After def tearDown(): Unit = hasher = null

    @Test
    def computeHashTest(): Unit = {
        val string1 = "Hello"
        val string2 = "world"
        assertNotEquals(hasher.computeStringHash(string1, "SHA-256"), hasher.computeStringHash(string2, "SHA-256"))
        assertEquals(hasher.computeStringHash(string1, "SHA-256"), hasher.computeStringHash(string1, "SHA-256"))
    }

}
