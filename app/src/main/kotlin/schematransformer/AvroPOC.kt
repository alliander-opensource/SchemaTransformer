package schematransformer

import org.apache.avro.Schema
import org.apache.avro.SchemaBuilder
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.eclipse.rdf4j.model.vocabulary.SHACL
import schematransformer.read.readDirectory
import java.io.File


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

    val m = readDirectory(File("app/src/test/resources/rdfs/"))
    val nodeShapeIRIs = m.data.filter(null, RDF.TYPE, SHACL.NODE_SHAPE).subjects()

    for (nodeShapeIRI in nodeShapeIRIs) {
        if (!nodeShapeIRI.toString().endsWith("DShape")) continue  // Let's build just the D shape for now.

        val targetClassIRI = m.data.filter(nodeShapeIRI, SHACL.TARGET_CLASS, null).objects().first()  // Assume it's one.
        val targetClass = when (targetClassIRI) {
            is IRI -> targetClassIRI.localName
            else -> throw IllegalStateException()
        }
        val aliases = m.data.filter(targetClassIRI, RDFS.COMMENT, null).objects().first()
        val doc = m.data.filter(targetClassIRI, RDFS.LABEL, null).objects().first()

        val propertyIRIs = m.data.filter(nodeShapeIRI, SHACL.PROPERTY, null).objects()
        println(propertyIRIs)

    }
}
