package schematransformer

open class SchemaTransformerException(message: String) : Exception(message)

class NoRootObjectException(message: String = "No root object found.") : SchemaTransformerException(message)

class UnsupportedCardinalityException(
    message: String = "Unsupported `minCount` and `maxCount` for determining cardinality."
) : SchemaTransformerException(message)

class EnumSymbolsNotFoundException(
    message: String = "No symbols found for enum."
) : SchemaTransformerException(message)

class IncompatiblePropertyShapeException(
    message: String = "Property shape must contain either sh:node or sh:datatype."
) :
    SchemaTransformerException(message)
