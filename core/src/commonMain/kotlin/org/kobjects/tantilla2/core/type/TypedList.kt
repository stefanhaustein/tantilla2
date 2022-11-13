package org.kobjects.tantilla2.core.type

class TypedList(
    override val type: ListType,
    val data: MutableList<Any?> = mutableListOf<Any?>()
) : AbstractList<Any?>(), Typed {
    override val size: Int
        get() = data.size

    override fun get(index: Int) = data[index]

    operator fun set(index: Int, value: Any?) {
        data[index] = value
    }
}