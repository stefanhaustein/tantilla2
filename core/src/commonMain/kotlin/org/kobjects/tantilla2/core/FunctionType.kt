package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Bool
import org.kobjects.greenspun.core.Type
import org.kobjects.greenspun.core.Void

open class FunctionType(
    val isMethod: Boolean,
    val returnType: Type,
    val parameters: List<Parameter>,
) : Type {
    override fun toString(): String {
        val sb = StringBuilder("(")
        sb.append(parameters.joinToString { it.toString()  })
        sb.append(")")
        if (returnType != Void) {
            sb.append(" -> ").append(returnType.toString())
        }
        return sb.toString()
    }

    override val name: String
        get() = toString()
}