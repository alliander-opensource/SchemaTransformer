package schematransformer.avro

import org.apache.avro.Schema
import java.io.File

fun write(schema: Schema, output: File): Unit =
    output.writeText(schema.toString(true))