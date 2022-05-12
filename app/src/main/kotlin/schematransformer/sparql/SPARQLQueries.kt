package schematransformer.sparql

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.impl.BooleanLiteral
import org.eclipse.rdf4j.model.vocabulary.SHACL
import org.eclipse.rdf4j.model.vocabulary.SKOS
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection
import org.eclipse.rdf4j.sail.memory.model.IntegerMemLiteral
import schematransformer.type.NodeShape
import schematransformer.type.PropertyShape
import schematransformer.vocabulary.DXPROFILE

object SPARQLQueries {
    private fun from(vararg context: IRI): String = context.joinToString("\n") { "FROM <$it>" }

    fun getProfileResources(conn: SailRepositoryConnection, vararg context: IRI): Map<Value, List<Value>> =
        with(
            """
            PREFIX ${DXPROFILE.PREFIX}: <${DXPROFILE.NAMESPACE}>
            SELECT ?role ?artifact
            ${from(*context)}
            WHERE {
                ?prof rdf:type prof:Profile ;
                      prof:hasResource ?resource .
                      
                ?resource prof:hasRole ?role ;
                          prof:hasArtifact ?artifact .
            }""".trimIndent()
        ) {
            conn.prepareTupleQuery(this).evaluate()
                .groupBy({ it.getValue("role") }, { it.getValue("artifact") })
        }

    fun getRootObjectIRI(conn: SailRepositoryConnection, vararg context: IRI): IRI? =
        with(
            """
            PREFIX ${SHACL.PREFIX}: <${SHACL.NAMESPACE}>
            SELECT ?root
            ${from(*context)}
            WHERE {
                ?root a sh:NodeShape ;
                      rdfs:comment "RootObject" .
            }
        """.trimIndent()
        ) { conn.prepareTupleQuery(this).evaluate().firstOrNull()?.getValue("root") as IRI? }

    fun getNodeShape(
        conn: SailRepositoryConnection,
        nodeShapeIRI: IRI,
        withProperties: Boolean = true,
        vararg context: IRI
    ): NodeShape =
        with(
            """
            PREFIX ${SHACL.PREFIX}: <${SHACL.NAMESPACE}>
            PREFIX ${SKOS.PREFIX}: <${SKOS.NAMESPACE}>
            SELECT DISTINCT *
            ${from(*context)}
            WHERE {
                <$nodeShapeIRI> sh:targetClass ?targetClass .
                
                ?targetClass rdfs:label|skos:prefLabel ?label ;
                             rdfs:comment|skos:definition ?comment .
                
                OPTIONAL {
                    { <$nodeShapeIRI> sh:property ?property }
                    UNION
                    { <$nodeShapeIRI> (sh:and/rdf:rest*/rdf:first/sh:property)+ ?property }
                    
                ${if (withProperties) """
                    ?property sh:path ?propPath ;
                              sh:datatype|sh:node ?propRangeType .
                    BIND(EXISTS { ?property sh:node ?propRangeType } AS ?propIsNode)
                    
                    OPTIONAL {
                    ?propPath rdfs:label|skos:prefLabel ?propLabel ;
                              rdfs:comment|skos:definition ?propComment . }
                    OPTIONAL {
                    ?property sh:minCount ?propMinCount ;
                              sh:maxCount ?propMaxCount .
                    }
                """ else ""}
                }
        }
    """.trimIndent()
        ) {
            conn.prepareTupleQuery(this).evaluate()
                .map { row ->
                    row
                        .associate { it.name to it.value }
                        .filter { it.value != null }
                }.let { results ->
                    NodeShape(targetClass = results[0]["targetClass"] as IRI,
                        label = results[0]["label"]?.stringValue(),
                        comment = results[0]["comment"]?.stringValue(),
                        properties = results.associate {
                            it["property"]!!.stringValue() to PropertyShape(
                                path = it["propPath"] as Value,
                                node = null,
                                datatype = null,
//                                node = (if ((it.getValue("propIsNode") as BooleanLiteral).booleanValue())
//                                    getNodeShape(it.getValue("propRangeType") as IRI)
//                                else null),
//                                datatype = (if ((it.getValue("propIsNode") as BooleanLiteral).booleanValue())
//                                    null
//                                else it.getValue("propRangeType")),
                                label = it["propLabel"]?.stringValue(),
                                comment = it["propComment"]?.stringValue(),
                                minCount = (it["propMinCount"] as IntegerMemLiteral?)?.intValue(),
                                maxCount = (it["propMaxCount"] as IntegerMemLiteral?)?.intValue(),
                            )
                        }
                    )
                }
        }
}
