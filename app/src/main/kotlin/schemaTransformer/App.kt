package schemaTransformer

fun main(args: Array<String>) {
    val profiles = ProfileReader.read("app/src/main/resources/rdfs/ExampleProfile.ttl")
    //    val profiles = read("src/main/resources/testme.txt")

    println(profiles)
}
