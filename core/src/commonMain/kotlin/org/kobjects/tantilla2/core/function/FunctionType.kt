package org.kobjects.tantilla2.core.function

import org.kobjects.greenspun.core.Type
import org.kobjects.greenspun.core.Void
import tantillaName

open class FunctionType(
    val returnType: Type,
    val parameters: List<Parameter>,
) : Type {
    override fun toString(): String {
        val sb = StringBuilder("(")
        sb.append(parameters.joinToString { it.toString()  })
        sb.append(")")
        if (returnType != Void) {
            sb.append(" -> ").append(returnType.tantillaName)
        }
        return sb.toString()
    }
}