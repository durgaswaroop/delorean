object Delorean {
    def main(args: Array[String]): Unit = {
        val hasher: Hasher = new Hasher
        if (args.length > 0) {
            args.foreach(hasher.computeHashOfFile)
        }
        // hasher.computeHashOfFile("src/test/resources/test")
        // hasher.computeHashOfFile("src/test/resources/test_diff")
    }
}