package schematransformer

import picocli.CommandLine
import kotlin.system.exitProcess

fun main(args: Array<String>): Unit =
    exitProcess(CommandLine(SchemaTransformer()).execute(*args))
