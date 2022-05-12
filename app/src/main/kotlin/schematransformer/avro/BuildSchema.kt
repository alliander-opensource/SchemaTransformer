package schematransformer.avro

import org.apache.avro.Schema
import org.apache.avro.SchemaBuilder
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection
import schematransformer.NoRootObjectException
import schematransformer.sparql.SPARQLQueries
import schematransformer.util.getFileIRI
import schematransformer.vocabulary.DXPROFILE
import java.io.File


val primitivesMapping = mapOf(
    "string" to "string",
    "boolean" to "boolean",
    "decimal" to "bytes",
    "float" to "float",
    "double" to "double",
    "duration" to "fixed",
    "dateTime" to "string",
    "date" to "string",
    "time" to "string",
    "anyURI" to "string",
)


fun buildSchema(
    conn: SailRepositoryConnection,
    nodeShapeIRI: IRI,
    constraintsGraph: IRI,
    vararg vocabularyGraphs: IRI
): Schema {
    /* TODO.
    *  This is where the AVRO schema will be built.
    *  We will be:
    *    [ ] using the official `SchemaBuilder`
    *    [ ] using recursion, and paying mind to circular dependencies
    *    [ ] transforming cardinalities to AVRO compatible ones
    *    [ ] enums: sh:in; type = enum; symbols = ... (CShape is an example)
    */

    val nodeShape =
        SPARQLQueries.getNodeShape(conn, nodeShapeIRI, constraintsGraph, *vocabularyGraphs)

    val schema = SchemaBuilder.record(nodeShape.targetClass.localName)
        .doc(nodeShape.comment)
        .aliases(nodeShape.label)

    var fields = schema.fields()

    nodeShape.properties?.values?.forEach { p ->
        if (p.datatype != null) {
            fields = fields.name(p.path.localName).type(primitivesMapping[p.datatype.localName]).noDefault()

        } else if (p.node != null) {
            fields = fields.name(p.path.localName).type(buildSchema(conn, p.node, constraintsGraph, *vocabularyGraphs))
                .noDefault()
        } else {
            TODO() // Exception?
        }

    }
    return fields.endRecord()


//                    .name("id").type().stringType().noDefault()
//                    .name("def").type().unionOf().nullType().and().intType().endUnion().noDefault()
//                .endRecord()

}

fun buildSchemas(conn: SailRepositoryConnection, directory: File): MutableList<Schema> {
    val schemas = mutableListOf<Schema>()
    val artifactsByRole = SPARQLQueries.getProfileResources(conn)

    for (constraints in artifactsByRole[DXPROFILE.ROLE.CONSTRAINTS] ?: listOf()) {
        val constraintsFileURL = getFileIRI(directory, constraints.stringValue())
        val vocabularyFileURLs = artifactsByRole[DXPROFILE.ROLE.VOCABULARY]
            ?.map { getFileIRI(directory, it.stringValue()) }
            ?.toTypedArray() ?: arrayOf()

        val rootObjectIRI = SPARQLQueries.getRootObjectIRI(conn, constraintsFileURL)
            ?: throw NoRootObjectException("No root object found in constraints file: $constraintsFileURL")

        val schema = buildSchema(conn, rootObjectIRI, constraintsFileURL, *vocabularyFileURLs)
        schemas.add(schema)
    }

    return schemas
}