package schematransformer.logicalmodel.building

import org.eclipse.rdf4j.model.Model
import schematransformer.logicalmodel.LogicalModel
import schematransformer.logicalmodel.ProfileModel
import schematransformer.logicalmodel.RdfType
import schematransformer.logicalmodel.type


private fun buildLogicalModels(rdfModels: List<Model>): List<LogicalModel>? {
    val modelsByType = rdfModels.groupBy { it.type }
    val profiles = modelsByType[RdfType.Profile()] ?: return null

    return profiles.map { buildLogicalModel(it, modelsByType) }
}


private fun buildLogicalModel(
    profile: ProfileModel,
    associatedModelsByType: Map<RdfType, List<Model>>
): LogicalModel {
    TODO()
}

