package org.kobjects.tantilla2.core.type

class MutableTypedList(
    elementType: Type,
    override val data: MutableList<Any?> = mutableListOf<Any?>()
) : TypedList(elementType, data), Typed {

    operator fun set(index: Int, value: Any?) {
        data[index] = value
    }
}