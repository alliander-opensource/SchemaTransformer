package schematransformer

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.model.vocabulary.RDF
import schematransformer.vocabulary.DXPROFILE

fun Model.getProfileIRIs(): Set<Resource> = this.filter(null, RDF.TYPE, DXPROFILE.PROFILE).subjects()
fun Model.getResourceStatements(profileIri: IRI): List<Statement> =
    this.filter { it.subject in this.filter(profileIri, DXPROFILE.HASRESOURCE, null).objects() }