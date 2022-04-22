package schematransformer

import java.io.File
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.impl.TreeModel
import org.eclipse.rdf4j.model.util.Values
import org.eclipse.rdf4j.rio.Rio
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException

val SUPPORTED_FILE_EXTENSIONS = listOf("ttl", "rdf", "jsonld")

fun readFile(file: File): Model {
    val format =
        Rio.getParserFormatForFileName(file.name).orElseThrow {
            UnsupportedRDFormatException("Unrecognized file format.")
        }

    val context = Values.iri("iyrtpc:${file.name}") as Resource

    return Rio.parse(file.reader(), "", format, context)
}

fun readDirectory(directory: File): Model {
    val resultModel = TreeModel()

    for (file in directory.walk()) {
        if (file.extension !in SUPPORTED_FILE_EXTENSIONS) continue

        val model = readFile(file)
        resultModel.addAll(model)
    }

    return resultModel
}
