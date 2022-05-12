package schematransformer.util

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.util.Values
import java.io.File

fun getFileIRI(base: File, relativeURL: String): IRI =
    Values.iri(File(base, relativeURL).normalize().toURI().toString())