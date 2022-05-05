package schematransformer

import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.vocabulary.RDF
import schematransformer.vocabulary.DXPROFILE

fun RdfModel.getProfileIRIs(): Set<Resource> = this.data.filter(null, RDF.TYPE, DXPROFILE.PROFILE).subjects()

fun getProfileResources() = """
    PREFIX prof: <http://www.w3.org/ns/dx/prof/>
    SELECT ?role ?artifact
    WHERE {
        ?prof rdf:type prof:Profile ;
              prof:hasResource ?resource .
              
        ?resource prof:hasRole ?role ;
            prof:hasArtifact ?artifact .
    }""".trimIndent()