package schemaTransformer

fun main(args: Array<String>) {
    val profiles = ProfileReader.read("src/main/resources")
    //    val profiles = read("src/main/resources/testme.txt")

    println(profiles)
    println(ProfileReader.print())
}
