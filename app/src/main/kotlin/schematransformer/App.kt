package schematransformer

import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore
import schematransformer.avro.buildSchemas
import java.io.File
import schematransformer.read.readDirectory

fun main() {
    val directory = File("app/src/test/resources/rdfs")
    val model = readDirectory(directory)

    val db = SailRepository(MemoryStore())
    try {
        db.connection.use { conn ->
            conn.add(model.data)
            val schemas = buildSchemas(conn, model.path)
            println(schemas[0])
        }
    } finally {
        db.shutDown()
    }
}
