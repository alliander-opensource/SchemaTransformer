package old.logicalmodel.types

import old.logicalmodel.rdf.types.ConstraintsModel
import old.logicalmodel.rdf.types.ProfileModel
import old.logicalmodel.rdf.types.VocabularyModel

data class LogicalModel(
    val profile: ProfileModel,
    val constraints: List<ConstraintsModel>,
    val vocabularies: List<VocabularyModel>
)