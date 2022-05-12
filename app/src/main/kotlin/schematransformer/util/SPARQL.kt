package schematransformer.util

import org.eclipse.rdf4j.query.TupleQueryResult
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection

fun query(conn: SailRepositoryConnection, q: String): TupleQueryResult = conn.prepareTupleQuery(q).evaluate()