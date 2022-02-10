package schematransformer.rdf.types

import org.eclipse.rdf4j.model.Model

typealias RdfModel = Model

typealias ProfileModel = Model

typealias VocabularyModel = Model

typealias ConstraintsModel = Model

fun Model.isVocabulary(): Boolean = isProfile() or true

fun Model.isConstraints(): Boolean = isProfile() or true

fun Model.isProfile(): Boolean =
    this.any {
        it.predicate == RdfType.Profile().predicateIRI && it.`object` == RdfType.Profile().objectIRI
    }

val Model.type: RdfType
    get() =
        when {
            isProfile() -> RdfType.Profile()
            isConstraints() -> RdfType.Constraints()
            isVocabulary() -> RdfType.Vocabulary()
            else -> RdfType.Miscellaneous
        }
