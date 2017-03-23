package delorean.commands

import java.io.File
import java.util.logging.Logger

import delorean.FileOps._
import delorean.Hasher

/**
  * For the command 'add'.
  */
case class Add(files: List[String]) {
    val logger: Logger = Logger.getLogger(this.getClass.getName)
    logger.fine(s"Adding list of files $files.")
    val hasher = new Hasher
    var allFiles: List[String] = List.empty
    // If a directory is added, get all the files of the directory
    files.foreach(f ⇒
        if (new File(f).isDirectory) allFiles = allFiles ::: getFilesRecursively(f) else allFiles = f :: allFiles
    )
    // In the list only keep the names of the files.So
    allFiles = allFiles.filterNot(file ⇒ new File(file).isDirectory)
    logger.fine(s"After resolving all the files to be added (after deleting the directories) are: $allFiles")
    hasher.computeHashOfAddedFiles(allFiles)
}
