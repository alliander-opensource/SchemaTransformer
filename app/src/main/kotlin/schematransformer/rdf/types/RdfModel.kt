package schematransformer.rdf.types

import org.eclipse.rdf4j.model.Model

typealias RdfModel = Model

typealias ProfileModel = Model

typealias VocabularyModel = Model

typealias ConstraintsModel = Model

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
