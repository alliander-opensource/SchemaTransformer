package schematransformer

import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import schematransformer.dxprofile.LogicalModel
import schematransformer.dxprofile.readLogicalModel
import java.io.File
import kotlin.io.path.reader
import kotlin.io.path.Path


fun main() {
    val profiles = readLogicalModel(File("app/src/test/resources/rdfs/ExampleProfile.ttl"))
//    val profiles = ProfileReader.read("app/src/test/resources/rdfs/ExampleProfile.ttl")
//    val profiles = ProfileReader.read(Path("app/src/test/resources/ExampleProfile.png"))

//    val p = Path("app/src/test/resources/rdfs/ExampleVocabulary.ttl")
//
//    val m = Rio.parse(p.reader(), "", RDFFormat.TURTLE)
    println(profiles)
}