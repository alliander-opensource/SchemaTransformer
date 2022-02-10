package schematransformer.logicalmodel.types

import schematransformer.rdf.types.ConstraintsModel
import schematransformer.rdf.types.ProfileModel
import schematransformer.rdf.types.VocabularyModel

data class LogicalModel(
    val profile: ProfileModel,
    val constraints: List<ConstraintsModel>,
    val vocabularies: List<VocabularyModel>
)