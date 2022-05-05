package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.function.Lambda

class Adapter(
    val vmt: List<Lambda>,
    val instance: RuntimeContext,
)