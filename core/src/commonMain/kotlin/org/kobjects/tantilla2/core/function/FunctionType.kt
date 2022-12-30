package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.VoidType

interface FunctionType : Type {
    val returnType: Type
    val parameters: List<Parameter>

    fun isMethod() = parameters.size > 0 && parameters[0].name == "self"

    override fun serializeType(writer: CodeWriter, scope: Scope?) {
        writer.appendOpen('(')
        val mark = writer.mark()
        val rx = serializeTypeImpl(writer, scope, false, false)
        writer.unmark(mark)
        if (writer.x >= writer.lineLength - 1) {
            val multiLine = rx - mark.savedX + writer.indent.length + 3 >= writer.lineLength
            val twoLine = writer.x - mark.savedX + writer.indent.length + 3 >= writer.lineLength
            writer.indent()
            writer.reset(mark)
            writer.newline()
            serializeTypeImpl(writer, scope, twoLine, multiLine)
            writer.outdent()
        }
    }

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

    companion object {
    fun FunctionType.serializeTypeImpl(writer: CodeWriter, scope: Scope?, twoLine: Boolean, multiline: Boolean): Int {
        val startIndex = if (isMethod()) 1 else 0
        if (parameters.size > startIndex) {
            parameters[startIndex].serialize(writer, scope)
            for (i in startIndex + 1 until parameters.size) {
                if (multiline) {
                    writer.append(",")
                    writer.newline()
                } else {
                    writer.append(", ")
                }
                parameters[i].serialize(writer, scope)
            }
        }
        if (twoLine && returnType != VoidType) {
            writer.outdent()
            writer.newline()
            writer.indent()
        }
        val resultX = writer.x
        writer.appendClose(')')
        if (returnType != VoidType) {
            writer.append(" -> ")
            writer.appendType(returnType, scope)
        }
        return resultX
    }
    }

    open class Impl(override val returnType: Type, override val parameters: List<Parameter>) : FunctionType {

        override fun toString() = CodeWriter().appendType(this, null).toString()

        override fun equals(other: Any?) = other is FunctionType && isAssignableFrom(other)
    }
}