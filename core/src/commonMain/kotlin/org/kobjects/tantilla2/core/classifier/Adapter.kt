package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.function.Callable

class Adapter(
    val vmt: List<Callable>,
    val instance: Any?,
)