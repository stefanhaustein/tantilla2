package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.NoneType

object MutableSetType : SetType(
    "MutableSet",
    "A mutable set of elements.",
    { (it.get(0) as List<Any>).toMutableSet() }
) {

    init {
        defineMethod("add", "Adds an element to this set",
            NoneType,
            Parameter("value", ELEMENT_TYPE_PARAMETER)) {
            (it[0] as MutableSet<Any>).add(it[1]!!)
        }

        defineMethod("clear", "Remove all elements from this set.",
            NoneType
        ) {
            (it[0] as MutableSet<*>).clear()
        }

    }
}