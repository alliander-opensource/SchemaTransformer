package schematransformer.logicalmodel.building

import schematransformer.logicalmodel.types.LogicalModel
import schematransformer.rdf.types.ProfileModel
import schematransformer.rdf.types.RdfModel
import schematransformer.rdf.types.RdfType
import schematransformer.rdf.types.type

fun buildLogicalModels(rdfModels: List<RdfModel>): List<LogicalModel>? {
    val modelsByType = rdfModels.groupBy { it.type }

    return modelsByType[RdfType.Profile()]?.map { buildLogicalModel(it, modelsByType) }
}

fun buildLogicalModel(
    profile: ProfileModel,
    associatedModelsByType: Map<RdfType, List<RdfModel>>
): LogicalModel {
    val associatedVocabularies = TODO()
    val associatedConstraints = TODO()


    return LogicalModel(
        profile = profile,
        vocabularies = associatedVocabularies,
        constraints = associatedConstraints)
}