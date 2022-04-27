package schematransformer

import org.eclipse.rdf4j.model.IRI
import java.io.File
import schematransformer.read.readDirectory

fun main() {
    val directory = File("app/src/test/resources/rdfs")
    val model = readDirectory(directory)

    println(model)
    val profileIri = model.getProfileIRIs().first() as IRI  // We support only one profile per directory.
    val resourceStatements = model.getResourceStatements(profileIri)
    println(resourceStatements)

}
