package schematransformer.rdf.types

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.util.Values

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