package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.node.expression.FloatNode
import org.kobjects.tantilla2.core.type.FloatType
import org.kobjects.tantilla2.core.type.GenericType
import org.kobjects.tantilla2.core.type.IntType
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.stdlib.graphics.Color

open class SetType(
    val elementType: Type,
    name: String = "Set",
    docString: String = "An immutable set of elements.",
    ctor:  (LocalRuntimeContext) -> Any = { TypedSet(elementType, (it.get(0) as List<Any>).toSet()) }
) : NativeStructDefinition(
    null,
    name,
    docString,
    listOf(Parameter("elements", elementType, isVararg = true)),
    ctor
    ), CollectionType, GenericType {

    init {
        defineMethod("len", "Returns the size of this set", IntType) {
            (it[0] as TypedSet).size.toLong()
        }

    }

    override fun serializeType(writer: CodeWriter, scope: Scope?) {
        writer.append(name).append("[").appendType(elementType, scope).append("]")
    }

    override val genericParameterTypes: List<Type> = listOf(elementType)

    override fun create(types: List<Type>) = SetType(types.first())

    override fun isAssignableFrom(other: Type) = other is SetType && other.elementType == elementType

    override fun equals(other: Any?): Boolean =
        other is SetType && other.elementType == elementType && other !is MutableSetType
}