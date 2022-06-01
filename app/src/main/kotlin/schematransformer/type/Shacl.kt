package schematransformer.type

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Value

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
    val `in`: List<IRI?>?,
)

/* Voor Ritger.
    - Al die optionality, nested zelfs (zoals `in`). Is dit echt normaal of doe ik iets onhandig?
    - Geen mogelijkheid om te re-usen van properties
 */