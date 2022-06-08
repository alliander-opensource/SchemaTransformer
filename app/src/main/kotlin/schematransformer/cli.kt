package schematransformer

import schematransformer.read.readDirectory
import schematransformer.avro.buildSchemas
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore
import picocli.CommandLine.Option
import picocli.CommandLine.Command

import java.io.File;
import java.util.concurrent.Callable

@Command(name = "schema-transformer")
class SchemaTransformer: Callable<Int> {
    @Option(
        names = ["-c", "--config"],
        paramLabel = "CONFIG FILE",
        description = ["run config file for multiple profiles"]
    )
    lateinit var runConfig: File

    @Option(names = ["-b", "--base-path"], description = ["absolute base path, supply if you are using -c"])
    lateinit var basePath: String

    @Option(names = ["-p", "--dx-prof"], description = ["path to profile directory, use with single profile"])
    lateinit var inputFilePath: String

    @Option(names = ["-o", "--output"], description = ["path for file output"])
    lateinit var outPath: String

    var paths: Set<String> = setOf()

    override fun call(): Int {
        run()
        return 0
    }

    private fun run(): Int {
        val directory = File("app/src/test/resources/rdfs")
        val model = readDirectory(directory)

        val db = SailRepository(MemoryStore())
        try {
            db.connection.use { conn ->
                conn.add(model.data)


                val schemas = buildSchemas(conn, model.path)
                println(schemas[0])
                // WRITE
                return 0
            }
        } finally {
            db.shutDown()
        }
    }
}
