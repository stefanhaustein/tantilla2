package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.typeName

data class Parameter(
    val name: String,
    val type: Type,
) {
    override fun toString() = if (name == "self") "self" else "$name: ${type.typeName}"
}