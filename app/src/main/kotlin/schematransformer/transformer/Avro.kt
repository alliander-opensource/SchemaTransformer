package schematransformer.transformer

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.util.Values
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

    private fun fromNamed(vararg ctx: IRI): String = ctx.joinToString("\n") { "FROM NAMED <$it>" }

    fun fetchNodeShape(nodeShape: IRI, vararg contexts: IRI): String = """
        ${prefixes(SHACL(), SKOS())}
        
        SELECT ?targetClass ?label ?comment ?property ?path
        ${if (contexts.isNotEmpty()) fromNamed(*contexts) else ""}
        WHERE {
            <$nodeShape> sh:targetClass ?targetClass .
            
            { <$nodeShape> sh:property ?property . }
            UNION
            { <$nodeShape> (sh:and/rdf:rest*/rdf:first/sh:property)+ ?property }
            
            ?targetClass rdfs:label|skos:prefLabel ?label ;
                         rdfs:comment|skos:definition ?comment .
        }""".trimIndent()

    fun fetchPropertyShape(propertyShape: IRI, vararg contexts: IRI): String = """
        ${prefixes(SHACL(), SKOS())}
        
        SELECT ?path ?label ?rangeType ?comment ?minCount ?maxCount
        ${if (contexts.isNotEmpty()) fromNamed(*contexts) else ""}
        WHERE {
            <$propertyShape> sh:path ?path .
            { <$propertyShape> sh:datatype|sh:node ?rangeType . }
            OPTIONAL {
                ?path rdfs:label|skos:prefLabel ?label ;
                      rdfs:comment|skos:definition ?comment .
                <$propertyShape> sh:minCount ?minCount ;
                                 sh:maxCount ?maxCount .
             }
        }
    """.trimIndent()

    fun fetchRootObject(vararg contexts: IRI): String = """
        ${prefixes(SHACL())}
        
        SELECT ?root
        ${if (contexts.isNotEmpty()) fromNamed(*contexts) else ""}
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

    val rootObjectIRI =
        conn.prepareTupleQuery(ShaclQuery.fetchRootObject(constraintsNamedGraph)).evaluate()
            .flatten()
            .map { it.value as IRI }
            .firstOrNull()
            ?: return null

    val rootObjectQuery = ShaclQuery.fetchNodeShape(
        rootObjectIRI,
        constraintsNamedGraph,
        *vocabularyNamedGraphs.toTypedArray()
    )

    val schema = conn.prepareTupleQuery(rootObjectQuery).evaluate()
        .flatten()
        .groupBy({ it.name }, { it.value })
        .mapValues { it.value.distinct() }

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

        buildSchemaMap(conn, constraintsFileURL, vocabularyFileURLs)
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
            conn.add(model.data) // TODO: Can be done directly from file with syntax similar to `parse`.

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
