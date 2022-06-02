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
import kotlin.math.min


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
            *nodeShape.`in`?.map { it.localName }?.toTypedArray()
                ?: throw IllegalStateException("No enum symbols found.")
        )

fun transformCardinality(schema: Schema, minCount: Int, maxCount: Int): Schema {
    val baseSchema = SchemaBuilder.builder().type(schema);

    SchemaBuilder.builder().let { builder ->
        return when (minCount to maxCount) {
            1 to 1 -> baseSchema
            0 to 1 -> builder.unionOf().nullType().and().type(schema).endUnion();
            0 to Int.MAX_VALUE -> builder.unionOf().nullType().and().array().items().type(schema).endUnion()
            1 to Int.MAX_VALUE -> builder.array().items(schema)
            else -> throw IllegalStateException("Unsupported cardinality.")
        }
    }
}

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

    // Field generation.
    nodeShape.properties?.values?.forEach { p ->
        val normalizedMinCount = min(p.minCount ?: 0, 1)
        val normalizedMaxCount = with(p.maxCount ?: Int.MAX_VALUE) { if (this > 1) Int.MAX_VALUE else this }

        fields = when {
            p.datatype != null ->
                SchemaBuilder.builder().type(primitivesMapping[p.datatype.localName]).let { schema ->
                    fields.name(p.path.localName)
                        .doc(p.comment)
                        .type(transformCardinality(schema, normalizedMinCount, normalizedMaxCount))
                        .noDefault()
                }
            p.node != null ->
                if (p.node !in ancestorsPath)
                    fields.name(p.path.localName)
                        .doc(p.comment)
                        .type(
                            transformCardinality(
                                buildSchema(conn, p.node, constraintsGraph, ancestorsPath + p.node, *vocabularyGraphs),
                                normalizedMinCount,
                                normalizedMaxCount
                            )
                        )
                        .noDefault()
                else fields
            else -> throw IllegalStateException("Property shape must contain either sh:node or sh:datatype.")
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
    5. Repetition of field building

    TODO:
    - Field docs.
    - SOC: Separate out the DB stuff. Should be possible.
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