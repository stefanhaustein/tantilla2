package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.classifier.Generic
import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.function.Parameter

open class ListType(
    val elementType: Type,
    name: String = "List",
    ctor:  (LocalRuntimeContext) -> Any? = { TypedList(elementType, it.get(0) as List<Any?>) }
) : NativeStructDefinition(
    null,
    name,
    "An immutable list of elements.",
    listOf(Parameter("elements", elementType, isVararg = true)),
    ctor
    ), Generic {

    init {
        defineNativeFunction("len", "Returns the length of the list",
            IntType, Parameter("self", this)) {
            (it[0] as TypedList).size.toLong()
        }

        defineMethod("index", "Returns the index of the value in the list, or -1 if not found.",
            IntType, Parameter("value", elementType)) {
            (it[0] as TypedList).indexOf(it[1]).toLong()
        }

    }

    override fun serializeType(writer: CodeWriter, scope: Scope?) {
        writer.append(name).append("[").appendType(elementType, scope).append("]")
    }

    open fun create(size: Int, init: (Int) -> Any?) = TypedList(this, MutableList(size, init))

    override val genericParameterTypes: List<Type> = listOf(elementType)

    override fun create(types: List<Type>) = ListType(types.first())

    override fun isAssignableFrom(other: Type) = other is ListType && other.elementType == elementType

    override fun equals(other: Any?): Boolean =
        other is ListType && other.elementType == elementType && other !is MutableListType
}