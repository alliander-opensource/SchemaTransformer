package schematransformer.type

import org.apache.avro.Schema
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.vocabulary.XSD

val XsdToAvro: Map<IRI, Schema.Type> = mapOf(
    XSD.STRING to Schema.Type.STRING,
    XSD.BOOLEAN to Schema.Type.BOOLEAN,
    XSD.INT to Schema.Type.INT,
    XSD.DECIMAL to Schema.Type.BYTES,
    XSD.FLOAT to Schema.Type.FLOAT,
    XSD.DOUBLE to Schema.Type.DOUBLE,
    XSD.DURATION to Schema.Type.FIXED,
    XSD.DATETIME to Schema.Type.STRING,
    XSD.DATE to Schema.Type.STRING,
    XSD.TIME to Schema.Type.STRING,
    XSD.ANYURI to Schema.Type.STRING,
)