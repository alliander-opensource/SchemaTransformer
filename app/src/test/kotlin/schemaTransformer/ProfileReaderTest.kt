package schemaTransformer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.rio.RDFFormat
import kotlin.io.path.Path
import org.eclipse.rdf4j.rio.Rio
import java.nio.file.Path
import kotlin.io.path.reader

// Test data.
val ttlExample = Path("src/test/resources/rdfs/ExampleProfile.ttl")

// Helper functions.
private fun parseTtlFile(f: Path): Model = Rio.parse(f.reader(), "", RDFFormat.TURTLE)

class ProfileReaderTest : FunSpec ({
    test("Reading ttl file should return RDF model") {
        val expected = listOf(parseTtlFile(ttlExample))
        val actual = ProfileReader.read(ttlExample)

        expected shouldBe actual
    }
})