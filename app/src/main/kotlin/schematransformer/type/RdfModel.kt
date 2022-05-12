package schematransformer.type

import org.eclipse.rdf4j.model.Model
import java.io.File

data class RdfModel(val path: File, val data: Model)