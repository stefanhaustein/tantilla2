package org.kobjects.tantilla2.core.type

open class TypedList(
    val elementType: Type,
    open val data: List<Any?> = mutableListOf<Any?>()
) : AbstractList<Any?>(), Typed {
    override val type: Type
        get() = ListType(elementType)

    override val size: Int
        get() = data.size

    override fun get(index: Int) = data[index]

}