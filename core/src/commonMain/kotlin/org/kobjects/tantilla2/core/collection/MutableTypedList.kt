package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.Typed

class MutableTypedList(
    elementType: Type,
    override val data: MutableList<Any> = mutableListOf()
) : TypedList(elementType, data), Typed {

    operator fun set(index: Int, value: Any) {
        data[index] = value
    }


    override val type: Type
        get() = MutableListType(elementType)
}