import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class PitStopHashGeneratorTest {
    private static PitStopHashGenerator generator;
    private static String testFile, testCopyFile, testDiffFile;

    @BeforeClass
    public static void setUp() {
        generator = new PitStopHashGenerator();
        testFile = "src/test/resources/test";
        testCopyFile = "src/test/resources/test_copy";
        testDiffFile = "src/test/resources/test_diff";
    }

    @AfterClass
    public static void tearDown() {
        generator = null;
    }

    @Test
    public void getFileContentsAsStringTest() {
        String testFileContent = generator.getFileContentsAsString(testFile);
        String testCopyFileContent = generator.getFileContentsAsString(testCopyFile);
        String testDiffFileContent = generator.getFileContentsAsString(testDiffFile);
        assertEquals(testFileContent, testCopyFileContent);
        assertNotEquals(testCopyFileContent, testDiffFileContent);
    }

    @Test
    public void computeHashOfAStringTest() {
        assertEquals(generator.computeHash("delorean"), generator.computeHash("delorean"));
        assertNotEquals(generator.computeHash("doc"), generator.computeHash("marty"));
    }

    @Test
    public void computeHashOfAFileTest() {
        assertEquals(generator.computeHashOfAFile(testFile), generator.computeHashOfAFile(testFile));
        assertEquals(generator.computeHashOfAFile(testFile), generator.computeHashOfAFile(testCopyFile));
        assertNotEquals(generator.computeHashOfAFile(testFile), generator.computeHashOfAFile(testDiffFile));
    }

}