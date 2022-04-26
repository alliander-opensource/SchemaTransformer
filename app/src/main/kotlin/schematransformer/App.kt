package schematransformer

import org.eclipse.rdf4j.model.util.Values
import java.io.File
import schematransformer.read.readDirectory

fun main() {
    val directory = File("app/src/test/resources/rdfs")
    val model = readDirectory(directory, contextFn = { path -> Values.iri("iyrtpc:${path.name}") })


}
