package schematransformer.transformer

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.util.Values
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.eclipse.rdf4j.model.vocabulary.SHACL
import org.eclipse.rdf4j.model.vocabulary.SKOS
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection
import org.eclipse.rdf4j.sail.memory.MemoryStore
import schematransformer.getProfileResources
import schematransformer.read.readDirectory
import schematransformer.vocabulary.DXPROFILE
import java.io.File

object ShaclQuery {
    private fun prefixes(vararg vocab: Any): String =
        vocab.joinToString("\n") {  // TODO: Beautify. Also the mixed indent in the output.
            with(it.javaClass.declaredFields) {
                "PREFIX ${this.first { f -> f.name == "PREFIX" }.get(String)}: <${
                    this.first { f -> f.name == "NAMESPACE" }.get(String)
                }>"
            }
        }

    private fun from(vararg ctx: IRI): String = ctx.joinToString("\n") { "FROM <$it>" }

    fun fetchNodeShape(nodeShape: IRI, vararg contexts: IRI): String = """
        ${prefixes(SHACL(), SKOS())}
        
        SELECT ?targetClass ?label ?comment ?property ?path
        ${if (contexts.isNotEmpty()) from(*contexts) else ""}
        WHERE {
            <$nodeShape> sh:targetClass ?targetClass .
            
            { <$nodeShape> sh:property ?property . }
            UNION
            { <$nodeShape> (sh:and/rdf:rest*/rdf:first/sh:property)+ ?property }
            
            ?targetClass rdfs:label|skos:prefLabel ?label ;
                         rdfs:comment|skos:definition ?comment .
        }""".trimIndent()

    fun fetchPropertyShape(propertyShape: Value, vararg contexts: IRI): String {
        val x =
            with(
                when (propertyShape) {
                    is IRI -> "<$propertyShape>"
                    else -> propertyShape
                }
            ) {
                """
        ${prefixes(SHACL(), SKOS())}
        
        SELECT ?path ?label ?rangeType ?comment ?minCount ?maxCount
        ${if (contexts.isNotEmpty()) from(*contexts) else ""}
        WHERE {
            $this sh:path ?path .
            { $this sh:datatype|sh:node ?rangeType . }
            OPTIONAL {
                ?path rdfs:label|skos:prefLabel ?label ;
                      rdfs:comment|skos:definition ?comment .
                $this sh:minCount ?minCount ;
                                 sh:maxCount ?maxCount .
             }
        }
    """.trimIndent()
            }
        println(x)
        return x
    }

    fun fetchRootObject(vararg contexts: IRI): String = """
        ${prefixes(SHACL())}
        
        SELECT ?root
        ${if (contexts.isNotEmpty()) from(*contexts) else ""}
        WHERE {
            ?root a sh:NodeShape ;
                  rdfs:comment "RootObject" .
        }""".trimIndent()
}

fun getFileIRI(base: File, relativeURL: String): IRI =
    Values.iri(File(base, relativeURL).normalize().toURI().toString())

fun buildSchemaMap(
    conn: SailRepositoryConnection,
    constraintsNamedGraph: IRI,
    vocabularyNamedGraphs: List<IRI>
): String? {
    println("constraints: $constraintsNamedGraph")
    println("vocabs: $vocabularyNamedGraphs")

    val rootObjectIRIQuery = ShaclQuery.fetchRootObject(constraintsNamedGraph)

    val rootObjectIRI =
        conn.prepareTupleQuery(rootObjectIRIQuery).evaluate()
            .flatten()
            .map { it.value as IRI }
            .first()

//    val testQ = """
//    PREFIX sh: <http://www.w3.org/ns/shacl#>
//    SELECT ?rootObject
//    FROM <$constraintsNamedGraph>
//    WHERE {
//        ?rootObject a sh:NodeShape ;
//                    rdfs:comment "RootObject" .
//    }""".trimIndent()

//    val res = conn.prepareTupleQuery(testQ).evaluate()

    val activeGraph = vocabularyNamedGraphs.toTypedArray() + constraintsNamedGraph


    val rootObjectQuery = ShaclQuery.fetchNodeShape(rootObjectIRI, *activeGraph)

    val schema = conn.prepareTupleQuery(rootObjectQuery).evaluate()
        .flatten()
        .groupBy({ it.name }, { it.value })
        .mapValues { it.value.distinct() }
        .mapValues {
            if (it.key == "property") {
                conn.prepareTupleQuery(ShaclQuery.fetchPropertyShape(it.value.first(), *activeGraph)).evaluate()
                    .map { r -> r }
            } else it.value
        }

//    val enrichedSchema = schema["property"]?.map {
//        conn.prepareTupleQuery(ShaclQuery.fetchPropertyShape(it, *activeGraph)).evaluate().map { r -> r }
//    }

    return ""
}

fun buildSchemas(conn: SailRepositoryConnection, directory: File) {
    val resources = conn.prepareTupleQuery(getProfileResources()).evaluate()
    val artifactsByRole = resources
        .groupBy({ it.getValue("role") }, { it.getValue("artifact") })

    for (constraints in artifactsByRole[DXPROFILE.ROLE.CONSTRAINTS] ?: listOf()) {
        val constraintsFileURL = getFileIRI(directory, constraints.stringValue())
        val vocabularyFileURLs =
            artifactsByRole[DXPROFILE.ROLE.VOCABULARY]?.map { getFileIRI(directory, it.stringValue()) } ?: listOf()

        val schemaMap = buildSchemaMap(conn, constraintsFileURL, vocabularyFileURLs)
        // TODO: buildSchema(schemaMap)
    }
}

fun main() {
    val directory = File("app/src/test/resources/rdfs")
    val model = readDirectory(directory)

//    val property = Values.iri("https://w3id.org/schematransform/ExampleShape#idShape")
//    val q = ShaclQuery.fetchNodeShape(rootObject)
//    val q = ShaclQuery.fetchPropertyShape(property)
//    print(q)

    val db = SailRepository(MemoryStore())
    try {
        db.connection.use { conn ->
//            val f = conn.valueFactory
//            val ex = "http://example.com/"
//            conn.add(f.createIRI(ex, "bart"), RDF.TYPE, f.createLiteral("H"), f.createIRI(ex, "graph1"))
//            conn.add(f.createIRI(ex, "sterre"), RDF.TYPE, f.createLiteral("H"), f.createIRI(ex, "graph2"))
//
            conn.add(model.data) // TODO: Can be done directly from file with syntax similar to `parse`.
//
            buildSchemas(conn, model.path)


//            val preparedQuery = conn.prepareTupleQuery(q)
//            val result = preparedQuery.evaluate()

//            val nodeShapeB = preparedQuery.evaluate()
//                .map { res -> res.associateBy({ it.name }, { it.value }) }

//            val nodeShapeB = preparedQuery.evaluate()
//                .flatten()
//                .groupBy({ it.name }, { it.value })
//                .mapValues { it.value.distinct() }

//            println(nodeShapeB)
//            println(result.toMap())

        }
    } finally {
        db.shutDown()
    }
}
