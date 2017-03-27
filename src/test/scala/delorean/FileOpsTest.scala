/*
 * Developer: Swaroop <durgaswaroop@gmail.com>
 * Date: March 2017
 */

package delorean

import java.io.{File, PrintWriter}
import java.nio.file.{Files, Paths}

import delorean.FileOps._
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.{BeforeClass, Test}

import scala.collection.mutable

object FileOpsTest {
    @BeforeClass
    def callToRide(): Unit = {
        // This will make sure it creates all the required files for the test
        if (!Files.exists(Paths.get(TIME_MACHINE))) new delorean.commands.Ride
    }
}

class FileOpsTest {
    val resourcesFolder = "src/test/resources/"
    val testFile = resourcesFolder + "test"
    val testCopyFile = resourcesFolder + "test_copy"
    val testDiffFile = resourcesFolder + "test_diff"
    val outputFile = resourcesFolder + "out"
    val travelogueFile = resourcesFolder + ".tm/travelogue"

    @Test
    def getLinesOfFileTest(): Unit = {
        assertEquals(3, getLinesOfFile(testFile).length)
        assertEquals(3, getLinesOfFile(testCopyFile).length)
        assertEquals(1, getLinesOfFile(testDiffFile).length)
    }

    @Test
    def getFilesRecursivelyTest(): Unit = {
        val sep = File.separator
        assertTrue(getFilesRecursively("src").contains(s"src${sep}main${sep}scala${sep}delorean${sep}Delorean.scala"))
        assertTrue(getFilesRecursively("src").contains(s"src${sep}main${sep}scala${sep}delorean${sep}FileOps.scala"))
    }

    @Test
    def addHashesAndContentOfLinesToPoolTest(): Unit = {
        val stringPoolFile = ".tm/string_pool_test"
        // To empty the file
        new PrintWriter(stringPoolFile)

        val map1 = mutable.LinkedHashMap("0aiw4n" → "world", "81anf0" → "doc")
        addHashesAndContentOfLinesToPool(map1, stringPoolFile)
        assertEquals(map1.size, getLinesOfFile(stringPoolFile).length)

        // Pool file will grow every time. So, the second time the num of lines should be the sum of two
        val map2 = mutable.LinkedHashMap[String, String]()
        addHashesAndContentOfLinesToPool(map2, stringPoolFile)
        assertEquals(map1.size + map2.size, getLinesOfFile(stringPoolFile).length)

        // When an existing (hash -> string) pair is found. It will not be added again. So, "doc" will not be added.
        val map3 = mutable.LinkedHashMap[String, String]("81anf0" → "doc", "aw3edc" → "hello")
        addHashesAndContentOfLinesToPool(map3, stringPoolFile)
        assertEquals(map1.size + map2.size + map3.size - 1, getLinesOfFile(stringPoolFile).length)
    }

    @Test
    def writeMapToFileANDGetFileAsMapTest(): Unit = {
        val map1 = mutable.LinkedHashMap("1" → "one", "2" → "two")
        writeMapToFile(map1, outputFile)
        assertEquals(map1, getFileAsMap(outputFile))

        val map2 = mutable.LinkedHashMap[String, String]()
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
