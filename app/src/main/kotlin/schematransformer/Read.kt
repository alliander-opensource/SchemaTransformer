package schematransformer

import java.io.File
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.impl.TreeModel
import org.eclipse.rdf4j.model.util.Values
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.rio.Rio
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException
import schematransformer.vocabulary.DXPROFILE

val SUPPORTED_FILE_EXTENSIONS = listOf("ttl", "xml", "jsonld")
const val CONTEXT_NAMESPACE_PREFIX = "iyrptc"

fun generateContextFromFileName(file: File): IRI =
    Values.iri("${CONTEXT_NAMESPACE_PREFIX}:${file.name}")

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

private fun getResources(model: Model, profileIri: IRI): Any {
    val resourceIRIs =
        model.filter { it.subject == profileIri && it.predicate == DXPROFILE.HASRESOURCE }.map {
            it.`object`
        }

    return resourceIRIs.map { iri -> model.filter { st -> st.subject == iri } }
}

fun assignSchemaNamedGraphs(model: Model): Any {
    val profileIRIs =
        model.filter { it.predicate == RDF.TYPE && it.`object` == DXPROFILE.PROFILE }.map {
            it.subject
        }

    for (profileIri in profileIRIs) {
        val resources = getResources(model, profileIri as IRI)
        println()
    }

    return 1
}
