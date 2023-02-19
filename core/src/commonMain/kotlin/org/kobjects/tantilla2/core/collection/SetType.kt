package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.IntType
import org.kobjects.tantilla2.core.type.Type

open class SetType(
    val elementType: Type,
    name: String = "Set",
    docString: String = "An immutable set of elements.",
    override val unparameterized: Type? = null,
    ctor:  (LocalRuntimeContext) -> Any = { TypedSet(elementType, (it.get(0) as List<Any>).toSet()) }
) : NativeStructDefinition(
    null,
    name,
    docString,
    ctor,
    Parameter("elements", elementType, isVararg = true),
), CollectionType {

    init {
        defineMethod("len", "Returns the size of this set", IntType) {
            (it[0] as TypedSet).size.toLong()
        }

    }

    override val genericParameterTypes: List<Type> = listOf(elementType)

    override fun withGenericsResolved(genericTypeMap: GenericTypeMap) =
        SetType(genericTypeMap.resolve(elementType), unparameterized = unparameterized ?: this)

    override fun isAssignableFrom(other: Type) = other is SetType && other.elementType == elementType

    override fun equals(other: Any?): Boolean =
        other is SetType && other.elementType == elementType && other !is MutableSetType
}