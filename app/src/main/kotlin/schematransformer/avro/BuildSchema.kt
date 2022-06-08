package schematransformer.avro

import org.apache.avro.Schema
import org.apache.avro.SchemaBuilder
import org.apache.avro.SchemaBuilder.FieldAssembler
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection
import schematransformer.EnumSymbolsNotFoundException
import schematransformer.IncompatiblePropertyShapeException
import schematransformer.NoRootObjectException
import schematransformer.UnsupportedCardinalityException
import schematransformer.sparql.SPARQLQueries
import schematransformer.type.NodeShape
import schematransformer.type.PropertyShape
import schematransformer.type.XsdToAvro
import schematransformer.util.getFileIRI
import schematransformer.vocabulary.DXPROFILE
import java.io.File
import kotlin.math.min

fun buildEnumSchema(nodeShape: NodeShape): Schema =
    SchemaBuilder.enumeration(nodeShape.targetClass.localName)
        .symbols(
            *nodeShape.`in`?.map { it.localName }?.toTypedArray()
                ?: throw EnumSymbolsNotFoundException()
        )

fun transformCardinality(schema: Schema, minCount: Int, maxCount: Int): Schema {
    val baseSchema = SchemaBuilder.builder().type(schema)

    val normalizedMinCount = min(minCount, 1)
    val normalizedMaxCount = if (maxCount > 1) Int.MAX_VALUE else maxCount

    SchemaBuilder.builder().let { builder ->
        return when (normalizedMinCount to normalizedMaxCount) {
            1 to 1 -> baseSchema
            0 to 1 -> builder.unionOf().nullType().and().type(schema).endUnion()
            0 to Int.MAX_VALUE -> builder.unionOf().nullType().and().array().items().type(schema).endUnion()
            1 to Int.MAX_VALUE -> builder.array().items(schema)
            else -> throw UnsupportedCardinalityException()
        }
    }
}

fun buildRecordField(
    property: PropertyShape,
    fields: FieldAssembler<Schema>,
    schema: Schema
): FieldAssembler<Schema> =
    fields.name(property.path.localName)
        .doc(property.comment)
        .type(schema)
        .noDefault()

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
        fields = when {
            p.datatype != null -> SchemaBuilder.builder().type(Schema.create(XsdToAvro[p.datatype]))
            p.node != null ->
                if (p.node !in ancestorsPath) buildSchema(
                    conn,
                    p.node,
                    constraintsGraph,
                    ancestorsPath + p.node,
                    *vocabularyGraphs
                ) else
                    null
            else -> throw IncompatiblePropertyShapeException()
        }?.let { schema ->
            buildRecordField(
                p,
                fields,
                transformCardinality(schema, p.minCount ?: 0, p.maxCount ?: Int.MAX_VALUE)
            )
        } ?: fields
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

        val schema =
            buildSchema(conn, rootObjectIRI, constraintsFileURL, listOf(rootObjectIRI), *vocabularyFileURLs)
        schemas.add(schema)
    }

    return schemas
}