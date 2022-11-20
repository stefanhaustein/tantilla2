package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.definition.Definition

open class TantillaRuntimeException(
    val definition: Definition?,
    val node: Evaluable?,
    message: String?,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {

    // TODO: Make sure this renders all exceptions nicely.
    override fun toString() = if (message.isNullOrBlank()) super.toString() else message ?: ""

}