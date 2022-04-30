package org.kobjects.tantilla2.core.function

import org.kobjects.greenspun.core.Type
import tantillaName

data class Parameter(
    val name: String,
    val type: Type,
) {
    override fun toString() = if (name == "self") "self" else "$name: ${type.tantillaName}"
}