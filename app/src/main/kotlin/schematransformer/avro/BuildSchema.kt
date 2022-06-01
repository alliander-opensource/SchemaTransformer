package schematransformer.avro

import org.apache.avro.Schema
import org.apache.avro.SchemaBuilder
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection
import schematransformer.NoRootObjectException
import schematransformer.sparql.SPARQLQueries
import schematransformer.type.NodeShape
import schematransformer.util.getFileIRI
import schematransformer.vocabulary.DXPROFILE
import java.io.File


val primitivesMapping = mapOf(
    "string" to "string",
    "boolean" to "boolean",
    "int" to "int",
    "decimal" to "bytes",
    "float" to "float",
    "double" to "double",
    "duration" to "fixed",
    "dateTime" to "string",
    "date" to "string",
    "time" to "string",
    "anyURI" to "string",
)

fun buildEnumSchema(nodeShape: NodeShape): Schema =
    SchemaBuilder.enumeration(nodeShape.targetClass.localName)
        .symbols(
            *nodeShape.`in`?.map { it.localName }?.toTypedArray() ?: throw IllegalStateException()
        )

fun buildRecordSchema(
    conn: SailRepositoryConnection,
    nodeShape: NodeShape,
    constraintsGraph: IRI,
    ancestorsPath: List<IRI> = listOf(),
    vararg vocabularyGraphs: IRI
): Schema {
    val schema = SchemaBuilder.record(nodeShape.targetClass.localName)
        .doc(nodeShape.comment)
        .aliases(nodeShape.label)

    var fields = schema.fields()

    nodeShape.properties?.values?.forEach { p ->
        fields = if (p.datatype != null) {
            fields.name(p.path.localName).type(primitivesMapping[p.datatype.localName]).noDefault()

        } else if (p.node != null && p.node !in ancestorsPath) {
            fields.name(p.path.localName)
                .type(buildSchema(conn, p.node, constraintsGraph, ancestorsPath + p.node, *vocabularyGraphs))
                .noDefault()
        } else {
            return fields.endRecord()
        }

    }
    return fields.endRecord()
}


fun buildSchema(
    conn: SailRepositoryConnection,
    nodeShapeIRI: IRI,
    constraintsGraph: IRI,
    ancestorsPath: List<IRI> = listOf(),
    vararg vocabularyGraphs: IRI
): Schema =
    /*
    Clean up:
    1. More `when` expressions perhaps?
    2. More functions I think.
    3. Repetition of fields.endRecord
    4. All the mutation and imperative code: is there a different way?

    TODO:
    - Field docs.
     */

    SPARQLQueries.getNodeShape(conn, nodeShapeIRI, constraintsGraph, *vocabularyGraphs).let { nodeShape ->
        when {
            nodeShape.`in`?.isNotEmpty() == true -> buildEnumSchema(nodeShape)
            else -> buildRecordSchema(conn, nodeShape, constraintsGraph, ancestorsPath, *vocabularyGraphs)
        }
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

        val schema = buildSchema(conn, rootObjectIRI, constraintsFileURL, listOf(rootObjectIRI), *vocabularyFileURLs)
        schemas.add(schema)
    }

    return schemas
}