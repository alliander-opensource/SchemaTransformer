package schematransformer

import org.eclipse.rdf4j.model.util.Values
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery
import schematransformer.read.readDirectory
import java.io.File


fun testSparqlBuilder(): SelectQuery {
    val x = SparqlBuilder.`var`("x")
    val y = SparqlBuilder.`var`("y")

    val qs = Queries.SELECT(x, y).where(x.isA(y))

    return qs
}


fun main() {
    val directory = File("app/src/test/resources/rdfs")
    val m = readDirectory(directory, contextFn = { path -> Values.iri("iyrtpc:${path.name}") })

    // Via SparQLBuilder.
    val q = testSparqlBuilder().queryString

    // Via raw string.
//    val q = "SELECT ?x ?y WHERE {?x rdf:type ?y}"

    val db = SailRepository(MemoryStore())
    try {
        db.connection.use { conn ->
            conn.add(m) // TODO: Can be done directly from file with syntax similar to `parse`.

            val preparedQuery = conn.prepareTupleQuery(q)
            val result = preparedQuery.evaluate()

            for (st in result) {
                println(st)
            }

        }
    } finally {
        db.shutDown()
    }
}

