package schemaTransformer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ProfileReaderTest : FunSpec ({
    test("Reading ttl file should return RDF model") {
        "sammy".length shouldBe 5
    }
})