package schematransformer

import schematransformer.read.readDirectory
import schematransformer.avro.buildSchemas
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore
import picocli.CommandLine.Option
import picocli.CommandLine.Command
import schematransformer.avro.writeSchema

import java.io.File;
import java.util.concurrent.Callable

@Command(name = "schema-transformer")
class SchemaTransformer : Callable<Int> {
    @Option(
        names = ["-c", "--config"],
        paramLabel = "CONFIG FILE",
        description = ["run config file for multiple profiles"]
    )
    var runConfig: File? = null

    @Option(names = ["-b", "--base-path"], description = ["absolute base path, supply if you are using -c"])
    var basePath: String? = null

    @Option(names = ["-p", "--dx-prof"], description = ["path to profile directory, use with single profile"])
    var inputFilePath: String? = null

    @Option(names = ["-o", "--output"], required = true, description = ["path for file output"])
    lateinit var outPath: String

    var paths: MutableSet<String> = mutableSetOf()

    override fun call(): Int {
        run()
        return 0
    }

    private fun run(): Int {
        if (runConfig != null && basePath != null) {
            TODO()
        } else if (inputFilePath != null) {
            val model = readDirectory(File(inputFilePath!!))

            val db = SailRepository(MemoryStore())
            try {
                db.connection.use { conn ->
                    conn.add(model.data)

                    val schema = buildSchemas(conn, model.path)[0]
                    println(schema)
                    writeSchema(schema, File(outPath))
                    return 0
                }
            } finally {
                db.shutDown()
            }

        }
        return 1
    }
}
