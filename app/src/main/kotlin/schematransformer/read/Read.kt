package schematransformer.read

import java.io.File
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.impl.TreeModel
import org.eclipse.rdf4j.rio.Rio
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException

val SUPPORTED_FILE_EXTENSIONS = listOf("ttl", "xml", "jsonld")

fun readFile(file: File, contextFn: (file: File) -> IRI): Model {
    val format =
        Rio.getParserFormatForFileName(file.name).orElseThrow {
            UnsupportedRDFormatException("Unrecognized file format.")
        }

    return Rio.parse(file.reader(), "", format, contextFn(file))
}

fun readDirectory(
    directory: File,
    contextFn: (file: File) -> IRI,
    supportedFileExtensions: List<String> = SUPPORTED_FILE_EXTENSIONS
): Model {
    val resultModel = TreeModel()

    for (file in directory.walk()) {
        if (file.extension !in supportedFileExtensions) continue

        val model = readFile(file, contextFn)
        resultModel.addAll(model)
    }

    return resultModel
}
