package schematransformer

import java.io.File

fun main() {
    val directory = File("app/src/test/resources/rdfs")
    val m = readDirectory(directory, { generateContextFromFileName(it) })
    print(m)
}
