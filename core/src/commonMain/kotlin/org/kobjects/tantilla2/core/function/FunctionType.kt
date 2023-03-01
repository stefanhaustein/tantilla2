package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.definition.UserRootScope
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.NoneType
import org.kobjects.tantilla2.core.type.PlaceHolderType

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

    override fun withGenericsResolved(typeList: List<Type>): FunctionType {
        require(typeList.size == genericParameterTypes.size) {
            "Generic parameter types $genericParameterTypes size doesn't match type list size $typeList for $this"}
        if (genericParameterTypes.isEmpty()) {
            return this
        }
        val map = mutableMapOf<Type, Type>()
        for (i in typeList.indices) {
            map.put(genericParameterTypes[i], typeList[i])
        }
        return mapTypes { map[it] ?: it }
    }

    fun requireTraitMethodTypeMatch(name: String, traitMethodType: FunctionType) {

         if (!returnType.equalsIgnoringTypeVariables(traitMethodType.returnType)) {
            throw IllegalArgumentException("Return type ${returnType} for $name doesn't match trait method return type ${traitMethodType.returnType}")
        }

        if (parameters.size != traitMethodType.parameters.size) {
            throw IllegalArgumentException("$name has ${parameters.size} parameters in the implementation but ${traitMethodType.parameters.size} are required for the trait method.")
        }

        for (i in 1 until parameters.size) {
            if (!parameters[i].type.equalsIgnoringTypeVariables(traitMethodType.parameters[i].type)) {
                throw IllegalArgumentException("Parameter ${parameters[i]} of the implementation of $name doesn't match expected parameter type ${traitMethodType.parameters[i].type}")
            }
        }
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

    override fun mapTypes(mapping: (Type) -> Type): FunctionType {
        val resolvedParameters = List(parameters.size) {
            val parameter = parameters[it]
            val type = parameter.type.mapTypes(mapping)
            Parameter(parameter.name, type, parameter.defaultValueExpression, parameter.isVararg)
        }

        val resolvedReturnType = mapping(returnType)

        return Impl(resolvedReturnType, resolvedParameters)
    }

    override fun resolveGenericsImpl(
        actualType: Type,
        map: GenericTypeMap,
        allowNoneMatch: Boolean,
        allowAs: UserRootScope?,
    ): FunctionType {
        if (actualType !is FunctionType) {
            return super.resolveGenerics(actualType, map, allowNoneMatch, allowAs) as FunctionType
        }

        if (actualType.parameters.size != parameters.size) {
            throw IllegalArgumentException("Parameter count mismatch. expected: ${actualType.parameters.size} in $actualType; actual: ${parameters.size} in $this")
        }

        val resolvedParameters = List<Parameter>(parameters.size) {
            val parameter = parameters[it]
            val type = parameter.type.resolveGenerics(actualType.parameters[it].type, map)
            Parameter(parameter.name, type, parameter.defaultValueExpression, parameter.isVararg)
        }

        val resolvedReturnType = returnType.resolveGenerics(actualType.returnType, map)

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



    open class Impl(
        override val returnType: Type,
        override val parameters: List<Parameter>,
        override val genericParameterTypes: List<Type> = emptyList(),
    ) : FunctionType {

        override fun toString() = CodeWriter(forTitle = true).appendType(this).toString()

        override fun equals(other: Any?) = other is FunctionType && isAssignableFrom(other)

        override fun serializeType(writer: CodeWriter) = Companion.serializeType(this, writer)
    }
}