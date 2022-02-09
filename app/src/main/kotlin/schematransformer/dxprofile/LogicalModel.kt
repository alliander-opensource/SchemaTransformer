package schematransformer.dxprofile

import org.eclipse.rdf4j.model.Model

typealias DxProfile = Model

typealias Vocabulary = Model

typealias Constraints = Model

data class LogicalModel(
    val profile: DxProfile,
    val constraints: List<Constraints>,
    val vocabularies: List<Vocabulary>
)

fun LogicalModel.toAvro(): Nothing = TODO()