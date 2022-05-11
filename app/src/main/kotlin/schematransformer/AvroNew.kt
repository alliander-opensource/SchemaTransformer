package schematransformer

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.impl.BooleanLiteral
import org.eclipse.rdf4j.model.util.Values
import org.eclipse.rdf4j.model.vocabulary.SHACL
import org.eclipse.rdf4j.model.vocabulary.SKOS
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection
import org.eclipse.rdf4j.sail.memory.MemoryStore
import org.eclipse.rdf4j.sail.memory.model.MemIRI
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

    fun getRootObjectIRI(): IRI {
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

    private fun getNodeShape(nodeShapeIRI: IRI): Map<String, Any?> { // `?` because the type system cannot see my `filter` of non-null values.
        val query = """
            PREFIX sh: <http://www.w3.org/ns/shacl#>
            PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
            SELECT DISTINCT *
            ${activeGraphs.joinToString("\n") { "FROM <$it>" }}
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
            .map { row -> row.associate { it.name to it.value }.toMap() }

        val obj = mapOf(
            "targetClass" to results[0]["targetClass"],
            "comment" to results[0]["comment"],
            "label" to results[0]["label"],
            "property" to results.associate {
                it["property"].toString() to mutableMapOf(
                    "path" to it["propPath"],
                    "rangeType" to it["propRangeType"],
                    "isNode" to it["propIsNode"],
                    "label" to it["propLabel"],
                    "comment" to it["propComment"],
                    "minCount" to it["propMinCount"],
                    "maxCount" to it["propMaxCount"],
                ).filter { entry -> entry.value != null }
            }
        )

        return obj
    }

    fun build(nodeShapeIRI: IRI): Any {
        val nodeShape = getNodeShape(nodeShapeIRI)
        val properties = nodeShape["property"] as Map<String, MutableMap<Any, Any>>
        for (v in properties.values) {

            if (!(v["isNode"] as BooleanLiteral).booleanValue()) continue
            v["node"] = build(v["rangeType"] as MemIRI)
        }

    return nodeShape
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
            val schemaBuilder = SchemaBuilder(conn, constraintsIRI, arrayListOf(vocabularyIRI))
            val rootObjectIRI = schemaBuilder.getRootObjectIRI()
            val schema = schemaBuilder.build(rootObjectIRI)
            println(schema)
        }
    } finally {
        db.shutDown()
    }

}
