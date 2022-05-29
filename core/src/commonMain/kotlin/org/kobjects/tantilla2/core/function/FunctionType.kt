package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.runtime.Void

interface FunctionType :Type {
    val returnType: Type
    val parameters: List<Parameter>

    fun isMethod() = parameters.size > 0 && parameters[0].name == "self"

    override fun serializeType(writer: CodeWriter) {
        writer.append("(")
        if (parameters.size > 0) {
            parameters[0].serializeCode(writer)
            for (i in 1 until parameters.size) {
                writer.append(", ")
                parameters[i].serializeCode(writer)
            }
        }
        writer.append(")")
        if (returnType != Void) {
            writer.append(" -> ")
            writer.appendType(returnType)
        }
    }

    open class Impl(override val returnType: Type, override val parameters: List<Parameter>) : FunctionType {
    }


}