package schematransformer.type

import org.eclipse.rdf4j.model.IRI

data class PropertyShape(
    val path: IRI,
    val node: IRI?,
    val datatype: IRI?,
    val label: String?,
    val comment: String?,
    val minCount: Int?,
    val maxCount: Int?,
    val `in`: List<IRI>?,
)

data class NodeShape(
    val targetClass: IRI,
    val label: String?,
    val comment: String?,
    val properties: Map<String, PropertyShape>?,
    val `in`: List<IRI>?,
)