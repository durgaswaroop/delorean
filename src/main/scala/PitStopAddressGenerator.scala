import java.security.MessageDigest

import scala.io.Source

class PitStopAddressGenerator {

    // Hash for a List of files
    def computePitStopAddress(filePaths: List[String]) = {
        val concatenatedFileAddresses: StringBuilder = new StringBuilder
        filePaths.foreach(x ⇒ concatenatedFileAddresses.append(computeHashOfAFile(x)))
        println(concatenatedFileAddresses.toString)
        computeHashOfAString(concatenatedFileAddresses.toString)
    }

    // Address is the hash for a file
    def computeHashOfAFile(filePath: String) = computeHashOfAString(getFileContentsAsString(filePath))

    def computeHashOfAString(str: String) = MessageDigest.getInstance("SHA-256")
        .digest(str.getBytes).map("%02x".format(_)).mkString("")

    def getFileContentsAsString(filePath: String): String = Source.fromFile(filePath).mkString("")

}