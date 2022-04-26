package schematransformer

import io.kotest.core.spec.style.FunSpec
//import io.kotest.matchers.shouldBe
import java.nio.file.Path
import kotlin.io.path.reader
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio

import java.io.File

// Test data.
val ttlExample = File("src/test/resources/rdfs/ExampleProfile.ttl")

// Helper functions.
private fun parseRdfFile(f: File, format: RDFFormat = RDFFormat.TURTLE): Model =
    Rio.parse(f.reader(), "", format)

class ProfileReaderTest :
    FunSpec({
//        test("Reading ttl file should return RDF model") {
//            val expected = listOf(parseRdfFile(ttlExample))
//            val actual = read(ttlExample)
//
//            /* OWL Subject for first item has some randomness to it, so we check this one by hand for equality in their
//            predicate and object. */
//            expected[0].predicates() shouldBe actual[0].predicates()
//            expected[0].objects() shouldBe actual[0].objects()
//
//            expected.drop(1) shouldBe actual.drop(1)
//        }
    })
