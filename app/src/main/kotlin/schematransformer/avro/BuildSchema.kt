package schematransformer.avro

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.util.Values
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection
import schematransformer.NoRootObjectException
import schematransformer.sparql.SPARQLQueries
import schematransformer.util.getFileIRI
import schematransformer.util.query
import schematransformer.vocabulary.DXPROFILE
import java.io.File


typealias Schema = Any


fun buildSchema(
    conn: SailRepositoryConnection,
    rootObjectIRI: IRI,
    constraintsGraph: IRI,
    vararg vocabularyGraphs: IRI
): Schema {
    return rootObjectIRI as Schema
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