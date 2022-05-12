package old.avro

import com.github.avrokotlin.avro4k.Avro
import kotlinx.serialization.Serializable


@Serializable
data class Ingredient(val name: String?, val sugar: Double, val fat: Double)

@Serializable
data class Pizza(val name: String, val ingredients: List<Ingredient>, val vegetarian: Boolean, val kcals: Int)

fun main() {
    val schema = Avro.default.schema(Pizza.serializer())
    println(schema.toString(true))
}