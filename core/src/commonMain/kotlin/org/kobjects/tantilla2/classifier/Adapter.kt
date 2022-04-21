package org.kobjects.tantilla2.classifier

import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.function.Callable

class Adapter(
    val vmt: List<Callable>,
    val instance: RuntimeContext,
)