package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.builtin.RootScope
import org.kobjects.tantilla2.core.builtin.VoidType

interface FunctionType :Type {
    val returnType: Type
    val parameters: List<Parameter>

    fun isMethod() = parameters.size > 0 && parameters[0].name == "self"

    override fun serializeType(writer: CodeWriter, scope: Scope?) {
        val startIndex = if (isMethod()) 1 else 0
        writer.append("(")
        if (parameters.size > startIndex) {
            parameters[startIndex].serialize(writer, scope)
            for (i in startIndex + 1 until parameters.size) {
                writer.append(", ")
                parameters[i].serialize(writer, scope)
            }
        }
        writer.append(")")
        if (returnType != VoidType) {
            writer.append(" -> ")
            writer.appendType(returnType, scope)
        }
    }


    open class Impl(override val returnType: Type, override val parameters: List<Parameter>) : FunctionType {

        override fun toString() = CodeWriter().appendType(this, null).toString()

        override fun equals(other: Any?) =
            other is FunctionType
                    && other.returnType == returnType
                    && other.parameters == parameters


    }


}