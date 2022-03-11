package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Type
import typeToString

data class Parameter(
    val name: String,
    val type: Type,
) {
    override fun toString() = "$name: ${typeToString(type)}"
}