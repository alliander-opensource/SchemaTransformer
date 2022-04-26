package old.logicalmodel.building

import old.logicalmodel.types.LogicalModel
import old.logicalmodel.rdf.types.ProfileModel
import old.logicalmodel.rdf.types.RdfModel
import old.logicalmodel.rdf.types.RdfType
import old.logicalmodel.rdf.types.type

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