package schemaTransformer

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.streams.toList
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.rio.Rio

typealias ProfileFilePath = Path

object ProfileReader {
    fun read(path: String, recursive: Boolean = true): List<Model> = read(Path(path), recursive)

    fun read(path: Path, recursive: Boolean = true): List<Model> {
        val files = if (recursive) Files.walk(path) else Files.walk(path, 1)

        return files.filter { it.isRegularFile() }.map { parseFile(it) }.toList().filterNotNull()
    }

    private fun parseFile(filePath: ProfileFilePath): Model? {
        val rdfFormat = Rio.getParserFormatForFileName(filePath.toString()).unwrap()

        return rdfFormat?.let { Rio.parse(filePath.reader(), "", it) }
    }
}
