package org.kobjects.tantilla2.core.control

import org.kobjects.tantilla2.core.Evaluable
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.node.Node

open class TantillaRuntimeException(
    val definition: Definition?,
    val node: Node?,
    message: String?,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {

    // TODO: Make sure this renders all exceptions nicely.
    override fun toString() = if (message.isNullOrBlank()) super.toString() else message ?: ""

}