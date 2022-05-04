package schematransformer.transformer

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.util.Values
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore
import schematransformer.read.readDirectory
import java.io.File

object ShaclQuery {
    fun fetchNodeShape(nodeShape: IRI) = """
        PREFIX prof: <http://www.w3.org/ns/dx/prof/>
        PREFIX sh: <http://www.w3.org/ns/shacl#>
        PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
        
        SELECT ?otherShape ?targetClass ?label ?comment ?property ?path
        WHERE {
            <$nodeShape> sh:targetClass ?targetClass .
            
            { <$nodeShape> sh:property ?property . }
            UNION
            { <$nodeShape> (sh:and/rdf:rest*/rdf:first/sh:property)+ ?property }
            
            ?targetClass rdfs:label|skos:prefLabel ?label ;
                         rdfs:comment|skos:definition ?comment .
        }""".trimIndent()

    fun fetchPropertyShape(propertyShape: IRI) = """
        PREFIX prof: <http://www.w3.org/ns/dx/prof/>
        PREFIX sh: <http://www.w3.org/ns/shacl#>
        PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
        
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
    val m = readDirectory(directory)

    // Via SparQLBuilder.
//    val q = testSparqlBuilder().queryString
    val rootObjectIRI =
        m.first { it.predicate == RDFS.COMMENT && it.`object` == Values.literal("RootObject") }.subject as IRI
    val propertyIRI = Values.iri("https://w3id.org/schematransform/ExampleShape#idShape")
//    val q = ShaclQuery.fetchNodeShape(rootObjectIRI)
    val q = ShaclQuery.fetchPropertyShape(propertyIRI)

    // Via raw string.
//    val q = "SELECT ?x ?y WHERE {?x rdf:type ?y}"

    val db = SailRepository(MemoryStore())
    try {
        db.connection.use { conn ->
            conn.add(m) // TODO: Can be done directly from file with syntax similar to `parse`.

            val preparedQuery = conn.prepareTupleQuery(q)

            val result = preparedQuery.evaluate()
                .map { res -> res.associateBy({ it.name }, { it.value }) }

            println(result)

        }
    } finally {
        db.shutDown()
    }
}
