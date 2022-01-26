package schemaTransformer

import kotlin.io.path.Path


fun main(args: Array<String>) {
//    val profiles = ProfileReader.read("app/src/main/resources/rdfs/ExampleProfile.ttl")
//    val profiles = ProfileReader.read("app/src/main/resources/ExampleProfile.png")
    val profiles = ProfileReader.read(Path("app/src/main/resources/ExampleProfile.png"))

    println(profiles)

}
