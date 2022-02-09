package schematransformer.util

import java.util.Optional

fun <T> Optional<T>.unwrap(): T? = orElse(null)
