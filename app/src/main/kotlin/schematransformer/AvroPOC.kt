package schematransformer

import org.apache.avro.SchemaBuilder

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

    println(B)
}
