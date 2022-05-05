package schematransformer

import java.io.File
import schematransformer.read.readDirectory


fun main() {
    val directory = File("app/src/test/resources/rdfs")
    val model = readDirectory(directory)

    println(model)
}