package schematransformer

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.model.vocabulary.RDF
import schematransformer.vocabulary.DXPROFILE

private fun getResources(model: Model, profileIri: IRI): List<Statement> {
    val resourceIRIs =
        model
            .filter { it.subject == profileIri && it.predicate == DXPROFILE.HASRESOURCE }
            .map { it.`object` }

    return model.filter { st -> st.subject in resourceIRIs }
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
