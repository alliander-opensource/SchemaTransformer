package schematransformer.rdf.reading

import java.io.File
import java.io.Reader
import schematransformer.rdf.types.RdfFormat
import schematransformer.rdf.types.RdfModel

fun readRdfFiles(directory: String, recurse: Boolean = true): List<RdfModel> = readRdfFiles(File(directory), recurse)

fun readRdfFiles(directory: File, recurse: Boolean = true): List<RdfModel> {
    val files = directory.walk()
    if (!recurse) files.maxDepth(1)

    return readRdfFiles(files)
}

fun readRdfFiles(vararg files: File): List<RdfModel> = readRdfFiles(files.asSequence())

fun readRdfFiles(files: Sequence<File>): List<RdfModel> =
    readRdfFiles(
        files
            .mapNotNull { file -> getRdfFormat(file.toString())?.let { it to file.reader() } }
            .groupBy({ it.first }, { it.second }))

private fun readRdfFiles(readersByFormat: Map<RdfFormat, List<Reader>>): List<RdfModel> =
    readersByFormat.flatMap { (format, readers) -> readers.map { readRdfFile(it, format) } }

fun readRdfFile(file: String, format: RdfFormat): RdfModel = readRdfFile(File(file), format)

fun readRdfFile(file: File, format: RdfFormat): RdfModel = readRdfFile(file.reader(), format)

fun readRdfFile(reader: Reader, format: RdfFormat): RdfModel = readRdfFileWithRio(reader, format)