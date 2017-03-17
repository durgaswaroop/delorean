package delorean.commands

import delorean.Hasher

/**
  * For the command 'add'.
  */
case class Add(files: List[String]) {
    val hasher = new Hasher
    hasher.computeHashOfAddedFiles(files)
}
