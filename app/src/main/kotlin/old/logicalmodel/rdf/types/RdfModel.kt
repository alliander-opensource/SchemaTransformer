package old.logicalmodel.rdf.types

import org.eclipse.rdf4j.model.Model

typealias RdfModel = Model

typealias ProfileModel = Model

typealias VocabularyModel = Model

typealias ConstraintsModel = Model

val Model.isVocabulary: Boolean
    get() = isProfile or true

val Model.isConstraints: Boolean
    get() = isProfile or true

val Model.isProfile: Boolean
    get() =
        this.any { statement ->
            statement.predicate == RdfType.Profile().predicateIRI &&
                statement.`object` == RdfType.Profile().objectIRI
        }

val Model.type: RdfType
    get() =
        when {
            isProfile -> RdfType.Profile()
            isConstraints -> RdfType.Constraints()
            isVocabulary -> RdfType.Vocabulary()
            else -> RdfType.Miscellaneous
        }
