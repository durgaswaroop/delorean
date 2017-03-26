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

    val (realFiles, imaginaryFiles) = files.span(file ⇒ new File(file).exists())

    imaginaryFiles.foreach(file ⇒ println(s"delorean: File $file does not exist"))
    if (realFiles.isEmpty) System.exit(1)

    var filesToAdd: List[String] = realFiles

    // If a directory is added, get all the files of the directory
    files.foreach(f ⇒
        if (new File(f).isDirectory) filesToAdd = filesToAdd ::: getFilesRecursively(f) else filesToAdd = f :: filesToAdd
    )
    // In the list only keep the names of the files.So
    filesToAdd = filesToAdd.filterNot(file ⇒ new File(file).isDirectory)
    logger.fine(s"After resolving all the files to be added (after deleting the directories) are: $filesToAdd")
    hasher.computeHashOfAddedFiles(filesToAdd)
}
