package schemaTransformer

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.*
import kotlin.streams.toList
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio

typealias ProfileFilePath = Path

object ProfileReader {
    fun read(inputPath: String, recursive: Boolean = true): List<Model> {
        val path = Path(inputPath)
        val files: Stream<Path> = if (recursive) Files.walk(path) else Files.walk(path, 1)

        return files.filter { it.isRegularFile() }.map { readFile(it) }.toList()
    }

    private fun readFile(filePath: ProfileFilePath): Model {
        // TODO: Skip non-RDF files.

        val fileReader = filePath.reader()
        val f = Rio.parse(fileReader, "",RDFFormat.TURTLE)
//        fileReader.use { it.forEachLine { print(it) } }
        return f
    }
}
