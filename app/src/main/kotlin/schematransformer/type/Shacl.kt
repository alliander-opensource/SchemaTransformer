package schematransformer.type

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Value

data class PropertyShape(
    val path: Value,
    val node: IRI?,
    val datatype: Value?,
    val label: String?,
    val comment: String?,
    val minCount: Int?,
    val maxCount: Int?,
)

data class NodeShape(
    val targetClass: IRI,
    val label: String?,
    val comment: String?,
    val properties: Map<String, PropertyShape>?,
)