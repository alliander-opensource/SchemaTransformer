package schemaTransformer


fun main() {
    val profiles = ProfileReader.read("app/src/test/resources/rdfs/")
//    val profiles = ProfileReader.read("app/src/test/resources/rdfs/ExampleProfile.ttl")
//    val profiles = ProfileReader.read(Path("app/src/test/resources/ExampleProfile.png"))

    println(profiles[1])



}
