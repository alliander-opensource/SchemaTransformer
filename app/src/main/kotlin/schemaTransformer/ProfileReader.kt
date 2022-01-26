package schemaTransformer

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.*
import kotlin.streams.toList
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.rio.Rio

object ProfileReader {
    fun read(path: Path, recursive: Boolean = true): List<Model> {
        val files: Stream<Path> = if (recursive) Files.walk(path) else Files.walk(path, 1)

        return files.filter { it.isRegularFile() }.map { parseFile(it) }.toList().filterNotNull()
    }
    fun read(path: String, recursive: Boolean = true): List<Model> = read(Path(path), recursive)

    private fun parseFile(filePath: Path): Model? {
        val rdfFormat = Rio.getParserFormatForFileName(filePath.toString()).unwrap()

        return rdfFormat?.let { Rio.parse(filePath.reader(), "", it) }
    }
}
