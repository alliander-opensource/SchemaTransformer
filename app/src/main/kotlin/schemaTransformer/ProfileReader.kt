package schemaTransformer

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.*
import kotlin.streams.toList
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.rio.Rio

typealias ProfileFilePath = Path


object ProfileReader {
    fun read(inputPath: String, recursive: Boolean = true): List<Model?> {
        val path = Path(inputPath)
        val files: Stream<Path> = if (recursive) Files.walk(path) else Files.walk(path, 1)

        return files.filter { it.isRegularFile() }.map { parseFile(it) }.toList().filterNotNull()
    }

    private fun parseFile(filePath: ProfileFilePath): Model? {
        val rdfFormat = Rio.getParserFormatForFileName(filePath.toString()).unwrap()

        return rdfFormat?.let { Rio.parse(filePath.reader(), "", it) }
    }
}
