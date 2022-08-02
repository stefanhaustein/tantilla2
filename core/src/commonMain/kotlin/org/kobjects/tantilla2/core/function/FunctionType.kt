package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.runtime.Void

interface FunctionType :Type {
    val returnType: Type
    val parameters: List<Parameter>

    fun isMethod() = parameters.size > 0 && parameters[0].name == "self"

    override fun serializeType(writer: CodeWriter) {
        val startIndex = if (isMethod()) 1 else 0
        writer.append("(")
        if (parameters.size > startIndex) {
            parameters[startIndex].serializeCode(writer)
            for (i in startIndex + 1 until parameters.size) {
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

    fun serializeAbbreviatedType(writer: CodeWriter) {
        val startIndex = if (isMethod()) 1 else 0
        writer.append("(")
        if (parameters.size > startIndex) {
            writer.append(parameters[startIndex].name)
            for (i in startIndex + 1 until parameters.size) {
                writer.append(", ")
                writer.append(parameters[i].name)
            }
        }
        writer.append(")")
        if (returnType != Void) {
            writer.append(" -> ")
            writer.appendType(returnType)
        }
    }


    open class Impl(override val returnType: Type, override val parameters: List<Parameter>) : FunctionType {

        override fun toString() = CodeWriter().appendType(this).toString()

        override fun equals(other: Any?) =
            other is FunctionType
                    && other.returnType == returnType
                    && other.parameters == parameters


    }


}