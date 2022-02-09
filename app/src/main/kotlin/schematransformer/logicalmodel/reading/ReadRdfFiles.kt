package schematransformer.logicalmodel.reading

import java.io.File
import java.io.Reader
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import schematransformer.util.unwrap

fun readRdfFiles(directory: File, recurse: Boolean = true): List<Model> {
    val files = directory.walk()
    if (!recurse) files.maxDepth(1)

    return readRdfFiles(files)
}

fun readRdfFiles(vararg files: File): List<Model> = readRdfFiles(files.asSequence())

fun readRdfFiles(files: Sequence<File>): List<Model> =
    readRdfFiles(
        files
            .mapNotNull { file -> getRdfFormat(file.toString())?.let { it to file.reader() } }
            .groupBy({ it.first }, { it.second }))

fun readRdfFiles(readersByFormat: Map<RDFFormat, List<Reader>>): List<Model> =
    readersByFormat.flatMap { (format, readers) -> readers.map { readRdfFile(it, format) } }

fun readRdfFile(file: File, format: RDFFormat): Model = readRdfFile(file.reader(), format)

fun readRdfFile(reader: Reader, format: RDFFormat): Model = Rio.parse(reader, "", format)

private fun getRdfFormat(filename: String): RDFFormat? =
    Rio.getParserFormatForFileName(filename).unwrap()
