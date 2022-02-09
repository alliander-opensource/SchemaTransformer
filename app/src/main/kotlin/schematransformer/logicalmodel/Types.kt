package schematransformer.logicalmodel

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.util.Values

typealias ProfileModel = Model

typealias VocabularyModel = Model

typealias ConstraintsModel = Model

data class LogicalModel(
    val profile: ProfileModel,
    val constraints: List<ConstraintsModel>,
    val vocabularies: List<VocabularyModel>
)

sealed class RdfType {
    data class Profile(
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

fun Model.isProfile(): Boolean =
    this.any {
        it.predicate == RdfType.Profile().predicateIRI && it.`object` == RdfType.Profile().objectIRI
    }

val Model.type: RdfType
    get() =
        when {
            isConstraints() -> RdfType.Constraints()
            isProfile() -> RdfType.Profile()
            isVocabulary() -> RdfType.Vocabulary()
            else -> RdfType.Miscellaneous
        }
