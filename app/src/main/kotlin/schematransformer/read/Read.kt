package schematransformer.read

import java.io.File
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.impl.TreeModel
import org.eclipse.rdf4j.model.util.Values
import org.eclipse.rdf4j.rio.Rio
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException
import schematransformer.type.RdfModel

val SUPPORTED_FILE_EXTENSIONS = listOf("ttl", "xml", "jsonld")

fun readFile(file: File): Model {
    val format =
        Rio.getParserFormatForFileName(file.name).orElseThrow {
            UnsupportedRDFormatException("Unrecognized file format.")
        }
    val fileIRI = Values.iri(file.toURI().toString())

    return Rio.parse(file.reader(), "", format, fileIRI)
}

fun readDirectory(directory: File, supportedFileExtensions: List<String> = SUPPORTED_FILE_EXTENSIONS): RdfModel {
    val resultModel = TreeModel()

    for (file in directory.walk()) {
        if (file.extension !in supportedFileExtensions) continue

        val model = readFile(file)
        resultModel.addAll(model)
    }

    return RdfModel(path = directory.absoluteFile, data = resultModel)
}
