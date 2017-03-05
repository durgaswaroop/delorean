import java.io.{File, PrintWriter}
import java.nio.file.{Files, Paths}

import FileOps._
import org.junit.gen5.api.Assertions.{assertEquals, assertTrue}
import org.junit.gen5.api._

class FileOpsTest {
    val testFile = "src/test/resources/test"
    val testCopyFile = "src/test/resources/test_copy"
    val testDiffFile = "src/test/resources/test_diff"
    val outputFile = "src/test/resources/out"
    val travelogueFile = "src/test/resources/.tm/travelogue"

    @Test
    def getLinesOfFileTest(): Unit = {
        assertEquals(3, getLinesOfFile(testFile).length)
        assertEquals(3, getLinesOfFile(testCopyFile).length)
        assertEquals(1, getLinesOfFile(testDiffFile).length)
    }

    @Test
    def addHashesAndContentOfLinesToPoolTest(): Unit = {
        val stringPoolFile = ".tm/string_pool_test"
        // To empty the file
        new PrintWriter(stringPoolFile)

        val map1 = Map("0aiw4n" → "world", "81anf0" → "doc")
        addHashesAndContentOfLinesToPool(map1, stringPoolFile)
        assertEquals(map1.size, getLinesOfFile(stringPoolFile).length)

        // Pool file will grow every time. So, the second time the num of lines should be the sum of two
        val map2 = Map[String, String]()
        addHashesAndContentOfLinesToPool(map2, stringPoolFile)
        assertEquals(map1.size + map2.size, getLinesOfFile(stringPoolFile).length)

        // When an existing (hash -> string) pair is found. It will not be added again. So, "doc" will not be added.
        val map3 = Map[String, String]("81anf0" → "doc", "aw3edc" → "hello")
        addHashesAndContentOfLinesToPool(map3, stringPoolFile)
        assertEquals(map1.size + map2.size + map3.size - 1, getLinesOfFile(stringPoolFile).length)
    }

    @Test
    def writeMapToFileANDGetFileAsMapTest(): Unit = {
        val map1 = Map("1" → "one", "2" → "two")
        writeMapToFile(map1, outputFile)
        assertEquals(map1, getFileAsMap(outputFile))

        val map2 = Map[String, String]()
        writeMapToFile(map2, outputFile)
        assertEquals(map2, getFileAsMap(outputFile))
    }

    @Test
    def createIfDoesNotExistTest(): Unit = {
        val file = s"$outputFile.doc"
        createIfDoesNotExist(file)
        assertTrue(Files.exists(Paths.get(file)))
        new File(file).delete()
    }

    @Test
    def addLineHashesToHashesFileTest(): Unit = {
        val hashFileName = ".tm/hashes/abcdefghijklmnopqrstuvwxyz"
        val list2 = List()
        addLineHashesToHashesFile(list2, hashFileName)
        assertEquals(list2.length, getLinesOfFile(hashFileName).length)

        val list1 = List("abc123", "123abc")
        addLineHashesToHashesFile(list1, hashFileName)
        assertEquals(list1.length, getLinesOfFile(hashFileName).length)
    }
}
