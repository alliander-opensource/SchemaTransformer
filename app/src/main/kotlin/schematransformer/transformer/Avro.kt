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

    fun fetchNodeShape(nodeShape: IRI) = """
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

    fun fetchPropertyShape(propertyShape: IRI) = """
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
}

fun getFileIRI(base: File, relativeURL: String): IRI =
    Values.iri(File(base, relativeURL).normalize().toURI().toString())

fun buildSchema(conn: SailRepositoryConnection, constraintsURL: IRI, vocabularyURLs: List<IRI>): Any {
    println("constraints: $constraintsURL")
    println("vocabs: $vocabularyURLs")

    return 1
}

fun buildSchemas(conn: SailRepositoryConnection, model: RdfModel) {
    val resources = conn.prepareTupleQuery(getProfileResources()).evaluate()
    val artifactsByRole = resources
        .groupBy({ it.getValue("role") }, { it.getValue("artifact") })

    for (constraints in artifactsByRole[DXPROFILE.ROLE.CONSTRAINTS] ?: listOf()) {
        val constraintsFileURL = getFileIRI(model.path, constraints.stringValue())
        val vocabularyFileURLs =
            artifactsByRole[DXPROFILE.ROLE.VOCABULARY]?.map { getFileIRI(model.path, it.stringValue()) } ?: listOf()

        val rootObject = model.findRootObject(context = constraintsFileURL)
        println(rootObject)


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

            buildSchemas(conn, model)


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
