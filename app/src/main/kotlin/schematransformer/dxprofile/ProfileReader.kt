package schematransformer.dxprofile

import java.io.File
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.util.Values
import org.eclipse.rdf4j.rio.Rio
import schematransformer.util.unwrap

sealed class RdfType {
    data class DxProfile(
        val predicateIRI: IRI = Values.iri("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
        val objectIRI: IRI = Values.iri("http://www.w3.org/ns/dx/prof/Profile")
    ) : RdfType()
    data class Constraints(
        val roleIRI: IRI = Values.iri("http://www.w3.org/ns/dx/prof/role/constraints")
    ) : RdfType()
    data class Vocabulary(
        val roleIRI: IRI = Values.iri("http://www.w3.org/ns/dx/prof/role/vocabulary")
    ) : RdfType()
    object Miscellaneous : RdfType()
}

fun Model.isVocabulary(): Boolean = TODO()

fun Model.isConstraints(): Boolean = TODO()

fun Model.isDxProfile(): Boolean =
    this.any {
        it.predicate == RdfType.DxProfile().predicateIRI &&
            it.`object` == RdfType.DxProfile().objectIRI
    }

val Model.type: RdfType
    get() =
        when {
            isConstraints() -> RdfType.Constraints()
            isDxProfile() -> RdfType.DxProfile()
            isVocabulary() -> RdfType.Vocabulary()
            else -> RdfType.Miscellaneous
        }

//fun readLogicalModel(dir: File, recurse: Boolean = true): LogicalModel? {
//    val files = dir.walk()
//    if (recurse) files.maxDepth(1)
//
//    val rdfModels: Map<RdfType, List<Model>> =
//        files
//            .filter { it.isFile }
//            .map { readRdfFile(it) }
//            .filterNotNull()
//            .map { Pair(it.type, it) }
//            .toMap()
//    if (rdfModels.isEmpty()) return null
//
//    return LogicalModel(profile = profile, vocabularies = rdfModels[RdfType.Vocabulary], constraints = rdfModels[RdfType.Constraints()])
//}

private fun readRdfFile(file: File): Model? {
    val rdfFormat = Rio.getParserFormatForFileName(file.toString()).unwrap() ?: return null

    return Rio.parse(file.reader(), "", rdfFormat)
}
