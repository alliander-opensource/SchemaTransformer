package schematransformer.transformer

import arrow.core.computations.result
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.util.Values
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.eclipse.rdf4j.model.vocabulary.SHACL
import org.eclipse.rdf4j.model.vocabulary.SKOS
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore
import schematransformer.getProfileResources
import schematransformer.read.readDirectory
import schematransformer.toMap
import schematransformer.vocabulary.DXPROFILE
import java.io.File


fun Model.findRootObject(): IRI =
    this.first { it.predicate == RDFS.COMMENT && it.`object` == Values.literal("RootObject") }.subject as IRI


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

//    val rootObjectIRI =
//        model.first { it.predicate == RDFS.COMMENT && it.`object` == Values.literal("RootObject") }.subject

fun main() {
    val directory = File("app/src/test/resources/rdfs")
    val model = readDirectory(directory)

//    val property = Values.iri("https://w3id.org/schematransform/ExampleShape#idShape")
//    val rootObject = m.findRootObject()  // TODO: Must be done within context of relevant constraint file.
//    val q = ShaclQuery.fetchNodeShape(rootObject)
//    val q = ShaclQuery.fetchPropertyShape(property)
//    print(q)

    // Via raw string.
//    val q = "SELECT ?x ?y WHERE {?x rdf:type ?y}"

    val db = SailRepository(MemoryStore())
    try {
        db.connection.use { conn ->
            conn.add(model) // TODO: Can be done directly from file with syntax similar to `parse`.

            val results = conn.prepareTupleQuery(getProfileResources()).evaluate()
            val schemas = results
                .groupBy({ it.getValue("role") }, { it.getValue("artifact") })

            for (constraints in schemas.filterKeys { it == DXPROFILE.ROLE.CONSTRAINTS }.values.flatten() ) {
                println("constraints: $constraints")
                println("vocabs: ${schemas[DXPROFILE.ROLE.VOCABULARY]}")
            }



//            val preparedQuery = conn.prepareTupleQuery(q)
//            val result = preparedQuery.evaluate()

//            val nodeShapeB = preparedQuery.evaluate()
//                .map { res -> res.associateBy({ it.name }, { it.value }) }

//            val nodeShapeB = preparedQuery.evaluate()
//                .flatten()
//                .groupBy({ it.name }, { it.value })
//                .mapValues { it.value.distinct() }

//            println(result.toMap())

        }
    } finally {
        db.shutDown()
    }
}
