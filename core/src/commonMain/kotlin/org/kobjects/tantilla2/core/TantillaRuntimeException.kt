package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable

class TantillaRuntimeException(
    var definition: Definition?,
    val node: Evaluable<LocalRuntimeContext>?,
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)