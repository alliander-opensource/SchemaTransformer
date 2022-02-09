package schematransformer.logicalmodel.reading

import java.io.File
import java.io.Reader
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import schematransformer.util.unwrap

typealias RdfFilesMap = Map<RDFFormat, List<Model>>

fun readRdfFiles(directory: File, recurse: Boolean = true): RdfFilesMap {
    val files = directory.walk()
    if (!recurse) files.maxDepth(1)

    return readRdfFiles(files)
}

fun readRdfFiles(vararg files: File): RdfFilesMap = readRdfFiles(files.asSequence())

fun readRdfFiles(files: Sequence<File>): RdfFilesMap =
    readRdfFiles(
        files
            .mapNotNull { file -> getRdfFormat(file.toString())?.let { it to file.reader() } }
            .groupBy({ it.first }, { it.second }))

fun readRdfFiles(readersByFormat: Map<RDFFormat, List<Reader>>): RdfFilesMap =
    readersByFormat.mapValues { readers -> readers.value.map { readRdfFile(it, readers.key) } }

fun readRdfFile(file: File, format: RDFFormat): Model = readRdfFile(file.reader(), format)

fun readRdfFile(reader: Reader, format: RDFFormat): Model = Rio.parse(reader, "", format)

private fun getRdfFormat(filename: String): RDFFormat? =
    Rio.getParserFormatForFileName(filename).unwrap()

// fun Collection<Model>.mapByType(): Map<RdfType, List<Model>> = this.groupBy { it.type }

// private fun readRdfFile(reader: Reader, formatsToTry: List<RDFFormat> = listOf(RDFFormat.TURTLE,
// RDFFormat.RDFXML)): Model {
//    formatsToTry.forEach {
//        val model = try {
//            Rio.parse(reader, "", it)
//        }
//        catch ()
//    }
// }
//
