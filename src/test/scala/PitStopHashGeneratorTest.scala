import java.io.File
import java.nio.file.{Files, Paths}

import org.junit.Assert._
import org.junit._

class PitStopHashGeneratorTest {

    val testFile = "src/test/resources/test"
    val testCopyFile = "src/test/resources/test_copy"
    val testDiffFile = "src/test/resources/test_diff"
    val outputFile = "src/test/resources/out"
    val travelogueFile = "src/test/resources/.tm/travelogue"
    var generator: PitStopHashGenerator = _

    @Before def setUp(): Unit = generator = new PitStopHashGenerator

    @After def tearDown(): Unit = generator = null

    @Test
    def getLinesOfFileTest(): Unit = {
        assertEquals(3, generator.getLinesOfFile(testFile).length)
        assertEquals(3, generator.getLinesOfFile(testCopyFile).length)
        assertEquals(1, generator.getLinesOfFile(testDiffFile).length)
    }

    @Test
    def writeMapToFileANDGetFileAsMapTest(): Unit = {
        val map1 = Map("1" → "one", "2" → "two")
        generator.writeMapToFile(map1, outputFile)
        assertEquals(map1, generator.getFileAsMap(outputFile))

        val map2 = Map[String, String]()
        generator.writeMapToFile(map2, outputFile)
        assertEquals(map2, generator.getFileAsMap(outputFile))
    }

    @Test
    def createIfDoesNotExistTest(): Unit = {
        val file = s"$outputFile.doc"
        generator.createIfDoesNotExist(file)
        assertTrue(Files.exists(Paths.get(file)))
        new File(file).delete()
    }


}
