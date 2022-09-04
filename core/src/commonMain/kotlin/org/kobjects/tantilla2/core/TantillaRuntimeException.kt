package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable

class TantillaRuntimeException(
    val definition: Definition?,
    val node: Evaluable<LocalRuntimeContext>?,
    message: String?,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {

    // TODO: Make sure this renders all exceptions nicely.
    override fun toString() = if (message.isNullOrBlank()) super.toString() else message

}