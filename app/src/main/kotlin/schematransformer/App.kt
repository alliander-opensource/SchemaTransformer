package schematransformer

import schematransformer.rdf.reading.readRdfFiles
import java.io.File


fun main() {
    val profiles = readRdfFiles(File("app/src/test/resources/rdfs"))
//    val profiles = ProfileReader.read("app/src/test/resources/rdfs/ExampleProfile.ttl")
//    val profiles = ProfileReader.read(Path("app/src/test/resources/ExampleProfile.png"))

//    val p = Path("app/src/test/resources/rdfs/ExampleVocabulary.ttl")
//
//    val m = Rio.parse(p.reader(), "", RDFFormat.TURTLE)
    println(profiles)
}