package schematransformer.logicalmodel.building

import org.eclipse.rdf4j.model.Model
import schematransformer.logicalmodel.LogicalModel
import schematransformer.logicalmodel.ProfileModel
import schematransformer.logicalmodel.RdfType
import schematransformer.logicalmodel.type

private fun buildLogicalModels(rdfModels: List<Model>): List<LogicalModel>? {
    val modelsByType = rdfModels.groupBy { it.type }

    return modelsByType[RdfType.Profile()]?.map { buildLogicalModel(it, modelsByType) }
}

fun buildLogicalModel(
    profile: ProfileModel,
    associatedModelsByType: Map<RdfType, List<Model>>
): LogicalModel {
    val associatedVocabularies = TODO()
    val associatedConstraints = TODO()


    return LogicalModel(
        profile = profile,
        vocabularies = associatedVocabularies,
        constraints = associatedConstraints)
}
