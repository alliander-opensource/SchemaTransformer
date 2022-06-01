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


fun buildSchema(
    conn: SailRepositoryConnection,
    nodeShapeIRI: IRI,
    constraintsGraph: IRI,
    vararg vocabularyGraphs: IRI
): Schema {
    fun go(
        conn: SailRepositoryConnection,
        nodeShapeIRI: IRI,
        constraintsGraph: IRI,
        ancestorsPath: List<IRI> = listOf(),
        vararg vocabularyGraphs: IRI
    ): Schema {
        val nodeShape =
            SPARQLQueries.getNodeShape(conn, nodeShapeIRI, constraintsGraph, *vocabularyGraphs)

        if (nodeShape.`in` != null && nodeShape.`in`.isNotEmpty()) {
            return SchemaBuilder.enumeration(nodeShape.targetClass.localName)
                .symbols(*nodeShape.`in`.map { it.localName }.toTypedArray())
        } else {
            val schema = SchemaBuilder.record(nodeShape.targetClass.localName)
                .doc(nodeShape.comment)
                .aliases(nodeShape.label)

            var fields = schema.fields()

            nodeShape.properties?.values?.forEach { p ->
                fields = if (p.datatype != null) {
                    fields.name(p.path.localName).type(primitivesMapping[p.datatype.localName]).noDefault()

                } else if (p.node != null && p.node !in ancestorsPath) {
                    fields.name(p.path.localName)
                        .type(go(conn, p.node, constraintsGraph, ancestorsPath + p.node, *vocabularyGraphs))
                        .noDefault()
                } else {
                    return fields.endRecord()
                }

            }
            return fields.endRecord()
        }
    }
    return go(conn, nodeShapeIRI, constraintsGraph, listOf(nodeShapeIRI), *vocabularyGraphs)

}


//                    .name("id").type().stringType().noDefault()
//                    .name("def").type().unionOf().nullType().and().intType().endUnion().noDefault()
//                .endRecord()

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