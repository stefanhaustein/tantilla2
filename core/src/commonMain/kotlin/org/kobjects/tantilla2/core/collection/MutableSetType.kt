package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.NoneType

class MutableSetType(
    elementType: Type,
    unparameterized: MutableSetType? = null,
) : SetType(
    elementType,
    "MutableSet",
    "A mutable set of elements.",
    unparameterized,
    { MutableTypedSet(elementType, (it.get(0) as List<Any>).toMutableSet()) }
) {
    override fun withGenericsResolved(genericTypeMap: GenericTypeMap) =
        MutableSetType(genericTypeMap.resolve(elementType), this)

    override fun equals(other: Any?): Boolean =
        other is MutableSetType && other.elementType == elementType

    init {
        defineMethod("add", "Adds an element to this set",
            NoneType,
            Parameter("value", elementType)) {
            (it[0] as MutableTypedSet).data.add(it[1]!!)
        }

        defineMethod("clear", "Remove all elements from this set.",
            NoneType
        ) {
            (it[0] as MutableTypedSet).data.clear()
        }

    }
}