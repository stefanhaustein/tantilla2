package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.function.Parameter

class MutableListType(
    elementType: Type
) : ListType(
    elementType,
    "MutableList[${elementType.typeName}]"
) {


    override fun create(size: Int, init: (Int) -> Any?) = MutableTypedList(this, MutableList(size, init))

    init {
        defineMethod("append", "Appends an element to the list",
            VoidType,
            Parameter("value", elementType)) {
            (it[0] as MutableTypedList).data.add(it[1])
        }

        defineMethod("sort", "Sort this list in place.",
            VoidType) {
            ((it[0] as MutableTypedList).data as (MutableList<Comparable<Any>>)).sort()
        }
    }
}