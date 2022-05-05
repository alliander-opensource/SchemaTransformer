package schematransformer.transformer

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.util.Values
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.eclipse.rdf4j.model.vocabulary.SHACL
import org.eclipse.rdf4j.model.vocabulary.SKOS
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection
import org.eclipse.rdf4j.sail.memory.MemoryStore
import schematransformer.RdfModel
import schematransformer.getProfileResources
import schematransformer.read.readDirectory
import schematransformer.vocabulary.DXPROFILE
import java.io.File


fun RdfModel.findRootObject(context: IRI): IRI? {
    val matches = this.data.filter(null, RDFS.COMMENT, Values.literal("RootObject"), context)

    return if (matches.isNotEmpty()) matches.first().subject as IRI
    else null
}


object ShaclQuery {
    private fun prefixes(vararg vocab: Any): String =
        vocab.joinToString("\n") {  // TODO: Beautify. Also the mixed indent in the output.
            with(it.javaClass.declaredFields) {
                "PREFIX ${this.first { f -> f.name == "PREFIX" }.get(String)}: <${
                    this.first { f -> f.name == "NAMESPACE" }.get(String)
                }>"
            }
        }

    fun fetchNodeShape(nodeShape: IRI): String = """
        ${prefixes(SHACL(), SKOS())}
        
        SELECT ?targetClass ?label ?comment ?property ?path
        WHERE {
            <$nodeShape> sh:targetClass ?targetClass .
            
            { <$nodeShape> sh:property ?property . }
            UNION
            { <$nodeShape> (sh:and/rdf:rest*/rdf:first/sh:property)+ ?property }
            
            ?targetClass rdfs:label|skos:prefLabel ?label ;
                         rdfs:comment|skos:definition ?comment .
        }""".trimIndent()

    fun fetchPropertyShape(propertyShape: IRI): String = """
        ${prefixes(SHACL(), SKOS())}
        
        SELECT ?path ?label ?rangeType ?comment ?minCount ?maxCount
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

    fun fetchRootObject(context: IRI): String = """
        ${prefixes(SHACL())}
        
        SELECT ?root
        FROM <$context>
        WHERE {
            ?root a sh:NodeShape ;
                  rdfs:comment "RootObject" .
        }""".trimIndent()
}

fun getFileIRI(base: File, relativeURL: String): IRI =
    Values.iri(File(base, relativeURL).normalize().toURI().toString())

fun buildSchema(conn: SailRepositoryConnection, constraintsNamedGraph: IRI, vocabularyNamedGraphs: List<IRI>): String? {
    println("constraints: $constraintsNamedGraph")
    println("vocabs: $vocabularyNamedGraphs")

    val rootObject =
        conn.prepareTupleQuery(ShaclQuery.fetchRootObject(constraintsNamedGraph)).evaluate()
            .flatten()
            .firstOrNull()
            ?: return null

    println(rootObject)
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

        buildSchema(conn, constraintsFileURL, vocabularyFileURLs)
    }
}


//    val rootObjectIRI =
//        model.first { it.predicate == RDFS.COMMENT && it.`object` == Values.literal("RootObject") }.subject

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
