package old.logicalmodel.rdf.reading

import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import old.logicalmodel.util.unwrap
import java.io.Reader

fun readRdfFileWithRio(reader: Reader, format: RDFFormat): Model = Rio.parse(reader, "", format)

fun getRdfFormat(filename: String): RDFFormat? =
    Rio.getParserFormatForFileName(filename).unwrap()
