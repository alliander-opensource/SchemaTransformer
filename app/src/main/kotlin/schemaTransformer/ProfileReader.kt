package schemaTransformer

import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.rio.RDFFormat

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.*
import kotlin.streams.toList

typealias ProfileFilePath = Path

typealias RdfModel = String

object ProfileReader {
  fun read(inputPath: String, recursive: Boolean = true): List<RdfModel> {
    val path = Path(inputPath)
    val files: Stream<Path> = if (recursive) Files.walk(path) else Files.walk(path, 1)

    return files.filter { it.isRegularFile() }.map { readFile(it) }.toList()
  }

  fun print() {
    print(RDFFormat.RDFXML)
  }

  private fun readFile(filePath: ProfileFilePath): RdfModel {
    // TODO: Skip non-RDF files.

    val fileReader = filePath.reader()
    fileReader.use { it.forEachLine { print(it) } }

    return ""
  }

}

