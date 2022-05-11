package schematransformer

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.util.Values
import org.eclipse.rdf4j.model.vocabulary.SHACL
import org.eclipse.rdf4j.model.vocabulary.SKOS
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection
import org.eclipse.rdf4j.sail.memory.MemoryStore
import schematransformer.read.readDirectory
import schematransformer.transformer.BuildSchemaMap
import schematransformer.transformer.ShaclQuery
import schematransformer.transformer.buildSchemas
import schematransformer.transformer.getFileIRI
import schematransformer.vocabulary.DXPROFILE
import java.io.File

class SchemaBuilder(
    private val conn: SailRepositoryConnection,
    private val constraintsNamedGraph: IRI,
    private val vocabularyNamedGraphs: List<IRI>
) {
    private val activeGraphs
        get() =
            vocabularyNamedGraphs.toTypedArray() + constraintsNamedGraph

    private fun getRootObjectIRI(): IRI {
        val query = """
            PREFIX sh: <http://www.w3.org/ns/shacl#>
            SELECT ?root
            FROM <$constraintsNamedGraph>
            WHERE {
                ?root a sh:NodeShape ;
                      rdfs:comment "RootObject" .
            }""".trimIndent()

        return conn.prepareTupleQuery(query).evaluate().first().first().value as IRI
    }

    private fun getNodeShape(nodeShapeIRI: IRI): Any {
        val query = """
            PREFIX sh: <http://www.w3.org/ns/shacl#>
            PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
            SELECT DISTINCT *
            ${activeGraphs.map { "FROM <$it>" }.joinToString("\n")}
            WHERE {
                <$nodeShapeIRI> sh:targetClass ?targetClass .
                
                { <$nodeShapeIRI> sh:property ?property . }
                UNION
                { <$nodeShapeIRI> (sh:and/rdf:rest*/rdf:first/sh:property)+ ?property }
                
                ?targetClass rdfs:label|skos:prefLabel ?label ;
                             rdfs:comment|skos:definition ?comment .
                             
                ?property sh:path ?propPath ;
                          sh:datatype|sh:node ?propRangeType .
                BIND(EXISTS { ?property sh:node ?propRangeType } AS ?propIsNode)
                OPTIONAL {
                    ?propPath rdfs:label|skos:prefLabel ?propLabel ;
                              rdfs:comment|skos:definition ?propComment .
                    ?property sh:minCount ?propMinCount ;
                              sh:maxCount ?propMaxCount .
                }
            }""".trimIndent()

        val results = conn.prepareTupleQuery(query).evaluate()
            .map { row -> row.associate { it.name to it.value }.toMutableMap<Any, Any?>() }

        results.forEach { row ->
            row["property"] = mutableMapOf<Any, Any?>(
                "iri" to row["property"],
                "path" to row["propPath"],
                "rangeType" to row["propRangeType"],
                "isNode" to row["propIsNode"],
                "label" to row["propLabel"],
                "comment" to row["propComment"],
                "minCount" to row["propMinCount"],
                "maxCount" to row["propMaxCount"],
            )
            row.remove("propPath")
            row.remove("propRangeType")
            row.remove("propIsNode")
            row.remove("propLabel")
            row.remove("propComment")
            row.remove("propMinCount")
            row.remove("propMaxCount")
        }  // TODO: Delete those keys from the top-level.

        val obj = mapOf<Any, Any?>(
            "targetClass" to results[0]["targetClass"],
            "comment" to results[0]["comment"],
            "label" to results[0]["label"],
            "property" to results.associate { (it["property"] as LinkedHashMap<Any, Any?>)["iri"] to it["property"] }
        )

        println(obj)

        return results
    }

    fun build(nodeShape: IRI? = null): Any {
        val rootObjectIRI = getRootObjectIRI()
        val rootObj = getNodeShape(rootObjectIRI)
//        val nodeShapes = rootObj["property"]?.map { getPropertyShape(it) }
        return rootObj
    }
}


fun main() {
    val directory = File("app/src/test/resources/rdfs")
    val model = readDirectory(directory)

    val db = SailRepository(MemoryStore())
    try {
        db.connection.use { conn ->
            conn.add(model.data)
            val constraintsIRI =
                Values.iri("file:/home/bartkl/Programming/alliander-opensource/SchemaTransformer/app/src/test/resources/rdfs/ExampleShapeWithPropertyShape.ttl")
            val vocabularyIRI =
                Values.iri("file:/home/bartkl/Programming/alliander-opensource/SchemaTransformer/app/src/test/resources/rdfs/ExampleVocabulary.ttl")
            val rootObjectIRI = Values.iri("https://w3id.org/schematransform/ExampleShape#BShape")
            val schemaBuilder = SchemaBuilder(conn, constraintsIRI, arrayListOf(vocabularyIRI))

            val rootObject = schemaBuilder.build(rootObjectIRI)
        }
    } finally {
        db.shutDown()
    }

}
