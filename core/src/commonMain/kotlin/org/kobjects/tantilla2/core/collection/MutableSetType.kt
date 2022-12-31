package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.type.IntType
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.VoidType

class MutableSetType(
    elementType: Type
) : SetType(
    elementType,
    "MutableSet",
    "A mutable set of elements.",
    { MutableTypedSet(elementType, (it.get(0) as TypedList).data.toMutableSet()) }
) {
    override fun withGenericsResolved(types: List<Type>) = MutableSetType(types.first())

    override fun equals(other: Any?): Boolean =
        other is MutableSetType && other.elementType == elementType

    init {
        defineMethod("add", "Adds an element to this set",
            VoidType,
            Parameter("value", elementType)) {
            (it[0] as MutableTypedSet).data.add(it[1]!!)
        }

        defineMethod("clear", "Remove all elements from this set.",
            VoidType
        ) {
            (it[0] as MutableTypedSet).data.clear()
        }

    }
}