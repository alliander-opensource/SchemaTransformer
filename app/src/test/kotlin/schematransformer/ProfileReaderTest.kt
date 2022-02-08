package schematransformer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.reader
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import schematransformer.dxprofile.readDxProfile

// Test data.
val ttlExample = Path("src/test/resources/rdfs/ExampleProfile.ttl")

// Helper functions.
private fun parseTtlFile(f: Path): Model = Rio.parse(f.reader(), "", RDFFormat.TURTLE)

class ProfileReaderTest :
    FunSpec({
        test("Reading ttl file should return RDF model") {
            val expected = listOf(parseTtlFile(ttlExample))
            val actual = readDxProfile(ttlExample)

            /* OWL Subject for first item has some randomness to it, so we check this one by hand for equality in their
            predicate and object. */
            expected[0].predicates() shouldBe actual[0].predicates()
            expected[0].objects() shouldBe actual[0].objects()

            expected.drop(1) shouldBe actual.drop(1)
        }
    })
