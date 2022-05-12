package old.avro

import org.eclipse.rdf4j.query.TupleQueryResult
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery
import schematransformer.read.readDirectory
import java.io.File

fun TupleQueryResult.toMap(): Map<String, Any> =
    this
        .flatten()
        .groupBy({ it.name }, { it.value })
        .mapValues { it.value.distinct() }

fun testSparqlBuilder(): SelectQuery {
    val x = SparqlBuilder.`var`("x")
    val y = SparqlBuilder.`var`("y")

    val qs = Queries.SELECT(x, y).where(x.isA(y))

    return qs
}


fun main() {
    val directory = File("app/src/test/resources/rdfs")
    val m = readDirectory(directory)

    // Via SparQLBuilder.
//    val q = testSparqlBuilder().queryString
//    val q = MyQueries2.getProfileResources

    // Via raw string.
    val q = "SELECT ?x ?y WHERE {?x rdf:type ?y}"

    val db = SailRepository(MemoryStore())
    try {
        db.connection.use { conn ->
            conn.add(m.data) // TODO: Can be done directly from file with syntax similar to `parse`.

            val preparedQuery = conn.prepareTupleQuery(q)
            val result = preparedQuery.evaluate()

            println(result.toMap())
            println(result)

        }
    } finally {
        db.shutDown()
    }
}

