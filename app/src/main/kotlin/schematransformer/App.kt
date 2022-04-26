package schematransformer

import java.io.File

const val CONTEXT_NAMESPACE_PREFIX = "iyrptc"

fun main() {
    val directory = File("app/src/test/resources/rdfs")
    val m = readDirectory(directory, { generateContextFromFileName(CONTEXT_NAMESPACE_PREFIX, it) })
    print(m)
}
