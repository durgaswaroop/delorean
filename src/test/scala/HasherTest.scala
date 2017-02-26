import java.io.File
import java.nio.file.{Files, Paths}

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
    def getLinesOfFileTest(): Unit = {
        assertEquals(3, hasher.getLinesOfFile(testFile).length)
        assertEquals(3, hasher.getLinesOfFile(testCopyFile).length)
        assertEquals(1, hasher.getLinesOfFile(testDiffFile).length)
    }

    @Test
    def writeMapToFileANDGetFileAsMapTest(): Unit = {
        val map1 = Map("1" → "one", "2" → "two")
        hasher.writeMapToFile(map1, outputFile)
        assertEquals(map1, hasher.getFileAsMap(outputFile))

        val map2 = Map[String, String]()
        hasher.writeMapToFile(map2, outputFile)
        assertEquals(map2, hasher.getFileAsMap(outputFile))
    }

    @Test
    def createIfDoesNotExistTest(): Unit = {
        val file = s"$outputFile.doc"
        hasher.createIfDoesNotExist(file)
        assertTrue(Files.exists(Paths.get(file)))
        new File(file).delete()
    }

    @Test
    def computeHashTest(): Unit = {
        val string1 = "Hello"
        val string2 = "world"
        assertNotEquals(hasher.computeHash(string1, "SHA-256"), hasher.computeHash(string2, "SHA-256"))
        assertEquals(hasher.computeHash(string1, "SHA-256"), hasher.computeHash(string1, "SHA-256"))
    }

    @Test
    def addLineHashesToHashesFileTest(): Unit = {
        val hashFileName = ".tm/hashes/abcdefghijklmnopqrstuvwxyz"
        val list2 = List()
        hasher.addLineHashesToHashesFile(list2, hashFileName)
        assertEquals(list2.length, hasher.getLinesOfFile(hashFileName).length)

        val list1 = List("abc123", "123abc")
        hasher.addLineHashesToHashesFile(list1, hashFileName)
        assertEquals(list1.length, hasher.getLinesOfFile(hashFileName).length)
    }

    // @Test
    def addHashesAndContentOfLinesToPoolTest(): Unit = {
        val stringPoolFile = ".tm/string_pool_test"

        val map1 = Map("0aiw4n" → "world", "81anf0" → "doc")
        hasher.addHashesAndContentOfLinesToPool(map1, stringPoolFile)
        assertEquals(map1.size, hasher.getLinesOfFile(stringPoolFile).length)

        val map2 = Map[String, String]()
        hasher.addHashesAndContentOfLinesToPool(map2, stringPoolFile)
        assertEquals(map2.size, hasher.getLinesOfFile(stringPoolFile).length)
    }

    @Test
    def writeMapToFileTest(): Unit = {
        val map1 = Map("1" → "one", "2" → "two")
        hasher.writeMapToFile(map1, outputFile)
        assertEquals(map1.size, hasher.getLinesOfFile(outputFile).length)

        val map2 = Map[String, String]()
        hasher.writeMapToFile(map2, outputFile)
        assertEquals(map2.size, hasher.getLinesOfFile(outputFile).length)
    }

}
