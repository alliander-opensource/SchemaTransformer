package schematransformer

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.util.Values
import java.io.File
import schematransformer.read.readDirectory

fun main() {
    val directory = File("app/src/test/resources/rdfs")
    val model = readDirectory(directory)

    println(model)
    val p = model.getProfileIRIs().first()  // We support only one profile per directory.
    val rs = model.getResourceStatements(p as IRI)
    println(rs)

}
