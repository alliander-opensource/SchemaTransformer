package schemaTransformer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.rio.RDFFormat
import kotlin.io.path.Path
import org.eclipse.rdf4j.rio.Rio
import java.nio.file.Path
import kotlin.io.path.reader


private fun parseTtlFile(f: Path): Model = Rio.parse(f.reader(), "", RDFFormat.TURTLE)



val jemoeder = Path("").toAbsolutePath().toString()
val ttlExample = Path("src/test/resources/rdfs/ExampleProfile.ttl")

class ProfileReaderTest : FunSpec ({
    println(jemoeder)
    test("Reading ttl file should return RDF model") {
        listOf(parseTtlFile(ttlExample)) shouldBe ProfileReader.read(ttlExample)
    }
})