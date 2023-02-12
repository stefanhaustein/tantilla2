package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.NoneType

interface FunctionType : Type {
    val returnType: Type
    val parameters: List<Parameter>

    fun isMethod() = parameters.size > 0 && parameters[0].name == "self"

    fun hasRequiredParameters(): Boolean {
        for (parameter in parameters) {
            if (parameter.defaultValueExpression == null && !parameter.isVararg) {
                return true
            }
        }
        return false
    }

    override fun isAssignableFrom(other: Type): Boolean {
        if (other !is FunctionType) {
            return false
        }
        if (other.returnType != returnType) {
            return false
        }
        if (parameters.size != other.parameters.size) {
            return false
        }
        for (i in parameters.indices) {
            if (parameters[i].isVararg != other.parameters[i].isVararg
                || parameters[i].type != other.parameters[i].type) {
                return false
            }
        }
        return true
    }


    override fun resolveGenerics(actualType: Type?, map: GenericTypeMap, allowNoneMatch: Boolean, allowAs: Boolean): FunctionType {
        if (actualType == null) {
            val resolvedParameters = List<Parameter>(parameters.size) {
                val parameter = parameters[it]
                val type = parameter.type.resolveGenerics(null, map, false, false)
                Parameter(parameter.name, type, parameter.defaultValueExpression, parameter.isVararg)
            }

            val resolvedReturnType = returnType.resolveGenerics(null, map, false, false)

            return Impl(resolvedReturnType, resolvedParameters)
        }

        if (actualType !is FunctionType) {
            return super.resolveGenerics(actualType, map, allowNoneMatch, allowAs) as FunctionType
        }

        if (actualType.parameters.size != parameters.size) {
            throw IllegalArgumentException("Parameter count mismatch. expected: ${actualType.parameters.size} in $actualType; actual: ${parameters.size} in $this")
        }

        val resolvedParameters = List<Parameter>(parameters.size) {
            val parameter = parameters[it]
            val type = parameter.type.resolveGenerics(actualType.parameters[it].type, map, false, false)
            Parameter(parameter.name, type, parameter.defaultValueExpression, parameter.isVararg)
        }

        val resolvedReturnType = returnType.resolveGenerics(actualType.returnType, map, false, false)

        return Impl(resolvedReturnType, resolvedParameters)
    }



    companion object {
        fun FunctionType.serializeTypeImpl(
            writer: CodeWriter,
            closeOnNewLine: Boolean,
            multiline: Boolean
        ): Int {
            val startIndex = if (isMethod()) 1 else 0
            if (parameters.size > startIndex) {
                parameters[startIndex].serialize(writer)
                for (i in startIndex + 1 until parameters.size) {
                    if (multiline) {
                        writer.append(",")
                        writer.newline()
                    } else {
                        writer.append(", ")
                    }
                    parameters[i].serialize(writer)
                }
            }
            if (closeOnNewLine) {
                writer.outdent()
                writer.newline()
                writer.indent()
            }
            val resultX = writer.x
            writer.append(')')
            if (returnType != NoneType) {
                writer.append(" -> ")
                writer.appendType(returnType)
            }
            return resultX
        }


        fun serializeType(functionType: FunctionType, writer: CodeWriter) {
            writer.append('(')
            val mark = writer.mark()
            val rx = functionType.serializeTypeImpl(writer, false, false)
            writer.unmark(mark)
            if (writer.x >= writer.lineLength - 1) {
                val multiLine = rx - mark.savedX + writer.indent.length + 3 >= writer.lineLength
                val closeOnNewLine = !writer.forTitle
                        || writer.x - mark.savedX + writer.indent.length + 3 >= writer.lineLength
                writer.indent()
                writer.reset(mark)
                writer.newline()
                functionType.serializeTypeImpl(writer, closeOnNewLine, multiLine)
                writer.outdent()
            }
        }


    }



    open class Impl(override val returnType: Type, override val parameters: List<Parameter>) : FunctionType {

        override fun toString() = CodeWriter(forTitle = true).appendType(this).toString()

        override fun equals(other: Any?) = other is FunctionType && isAssignableFrom(other)

        override fun serializeType(writer: CodeWriter) = Companion.serializeType(this, writer)
    }
}