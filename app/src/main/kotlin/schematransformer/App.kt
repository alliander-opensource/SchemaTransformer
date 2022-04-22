package schematransformer

// import schematransformer.logicalmodel.building.buildLogicalModel

import java.io.File
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.util.Values
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio

//val dxProfProfile: IRI = Values.iri("http://www.w3.org/ns/dx/prof/Profile")
//val customIri: IRI = Values.iri("http://www.ifyoureadthispleasecomecontribute.org/")


//fun readDirectoryFP(inputDirectory: File): Model =
//    inputDirectory
//        .walk()
//        .filter { it.extension in SUPPORTED_FILES }
//        .map {
//            Rio.parse(
//                it.reader(), "", RDFFormat.TURTLE, Values.iri("iyrtpc:${it.name}") as Resource)
//        }
//        .reduce { acc, m -> (acc + m) as Model }

fun main() {
    val directory = File("app/src/test/resources/rdfs")

    //        val model = TreeModel()
    //    for (file in directory.walk()) {
    //        if (file.extension !in listOf("ttl", "rdf", "jsonld")) continue
    //
    //        val fileModel = Rio.parse(file.reader(), "", RDFFormat.TURTLE,
    // Values.iri("iyrtpc:${file.name}") as Resource)
    //        model.addAll(fileModel)
    //    }
    //    println(model)
    val m = readDirectory(directory)
    print(m)
}
//    return readRdfFiles(files)

//    val builder = ModelBuilder()
//    builder.namedGraph()
