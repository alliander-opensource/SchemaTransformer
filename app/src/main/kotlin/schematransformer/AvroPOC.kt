package schematransformer

import org.apache.avro.Schema
import org.apache.avro.SchemaBuilder
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Model


fun buildSchema(model: Model, rootNodeShape: IRI): Schema {
    TODO()
}

fun main() {
    val D = SchemaBuilder.record("D")
        .doc("This is yet another class")
        .aliases("Dd")
        .fields()
        .name("id").type().stringType().noDefault()
        .name("def").type().unionOf().nullType().and().intType().endUnion().noDefault()
        .endRecord()
    val FromBtoDType = SchemaBuilder.unionOf().nullType().and().type(D).endUnion()

    val B = SchemaBuilder.record("B")
        .doc("This is a sub-class")
        .aliases("Bb")
        .fields()
        .name("FromBtoD").type(FromBtoDType).noDefault()
        .name("FromBtoDButSomehowDifferent").type(D).noDefault()
        .endRecord()

    val f = Schema.Field("BtoD", FromBtoDType)
    println(f)

    // TODO: Fix dat de docs worden opgepikt in de fields.
    // Kan blijkbaar niet met de builder? Rick doet het achteraf met een map.

    println(B)
}
