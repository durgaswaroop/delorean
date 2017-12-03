package delorean

import java.io.File
import java.nio.file.{Files, Paths}

// class created to group the indicator and its hash so that we can put it in a json as Gson doesn't work
// with classes defined inside methods
case class IndicatorAndHash(indicator: String, hash: String)

object IndicatorAndHash {
  def apply(file: File): IndicatorAndHash = {
    val lines = Files.readAllLines(Paths.get(file.getAbsolutePath))
    val hash = if (lines.isEmpty) "" else lines.get(0)
    new IndicatorAndHash(file.getName, hash)
  }
}
