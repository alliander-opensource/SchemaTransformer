package schematransformer.vocabulary

import org.eclipse.rdf4j.model.Namespace
import org.eclipse.rdf4j.model.util.Values

object DXPROFILE {
    val PREFIX = "prof"
    val NS: Namespace = Values.namespace("prof","http://www.w3.org/ns/dx/prof/")
    val PROFILE = Values.iri("http://www.w3.org/ns/dx/prof/Profile")    // TODO: Repetition of `Values.iri` is annoying.
    val HASRESOURCE = Values.iri("http://www.w3.org/ns/dx/prof/hasResource")
    val HASARTIFACT = Values.iri("http://www.w3.org/ns/dx/prof/hasArtifact")
    val HASROLE = Values.iri("http://www.w3.org/ns/dx/prof/hasRole")

    object ROLE {
        val CONSTRAINTS = Values.iri("http://www.w3.org/ns/dx/prof/role/constraints")
        val VOCABULARY = Values.iri("http://www.w3.org/ns/dx/prof/role/vocabulary")
    }
}