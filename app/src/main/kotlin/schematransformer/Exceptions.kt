package schematransformer

open class SchemaTransformerException(message: String) : Exception(message)

class NoRootObjectException(message: String = "No root object found.") : SchemaTransformerException(message)

class UnsupportedCardinalityException(
    message: String = "Unsupported `sh:minCount` and `sh:maxCount` for determining cardinality."
) : SchemaTransformerException(message)

class EnumSymbolsNotFoundException(
    message: String = "No symbols found for enumeration schema."
) : SchemaTransformerException(message)

class IncompatiblePropertyShapeException(
    message: String = "Property shape must contain either `sh:node` or `sh:datatype`."
) :
    SchemaTransformerException(message)
