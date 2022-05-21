package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.runtime.Void
import org.kobjects.tantilla2.core.typeName

open class FunctionType(
    val returnType: Type,
    val parameters: List<Parameter>,
) : Type {
    override fun toString(): String {
        val sb = StringBuilder("(")
        sb.append(parameters.joinToString { it.toString()  })
        sb.append(")")
        if (returnType != Void) {
            sb.append(" -> ").append(returnType.typeName)
        }
        return sb.toString()
    }
}