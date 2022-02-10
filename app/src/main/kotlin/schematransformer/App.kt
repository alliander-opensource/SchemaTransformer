package schematransformer

//import schematransformer.logicalmodel.building.buildLogicalModel
import schematransformer.logicalmodel.building.buildLogicalModels
import schematransformer.rdf.reading.readRdfFile
import schematransformer.rdf.reading.readRdfFiles
import java.io.File


fun main() {
//    val rdfFiles = readRdfFiles(File("app/src/test/resources/rdfs"))
//    val logicalModels = buildLogicalModels(rdfFiles)
    val profiles = readRdfFiles("app/src/test/resources/rdfs/ExampleShapeWithPropertyShape.ttl")
//    val profiles = readRdfFile("app/src/test/resources/rdfs/ExampleProfile.ttl")
    println(profiles)
//    val profiles = ProfileReader.read(Path("app/src/test/resources/ExampleProfile.png"))

//    val p = Path("app/src/test/resources/rdfs/ExampleVocabulary.ttl")
//
//    val m = Rio.parse(p.reader(), "", RDFFormat.TURTLE)
//    println(logicalModels)
}