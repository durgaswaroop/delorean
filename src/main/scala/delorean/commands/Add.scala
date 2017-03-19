package delorean.commands

import java.io.File

import delorean.FileOps._
import delorean.Hasher

/**
  * For the command 'add'.
  */
case class Add(files: List[String]) {
    val hasher = new Hasher
    var allFiles: List[String] = List[String]()
    // If a directory is added, get all the files of the directory
    files.foreach(f ⇒
        if (new File(f).isDirectory) allFiles = allFiles ::: getFilesRecursively(f) else allFiles = f :: allFiles
    )
    // In the list only keep the names of the files.So
    allFiles = allFiles.filterNot(file ⇒ new File(file).isDirectory)
    hasher.computeHashOfAddedFiles(allFiles)
}
