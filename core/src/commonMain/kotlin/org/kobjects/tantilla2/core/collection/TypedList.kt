package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.Typed

open class TypedList(
    val elementType: Type,
    open val data: List<Any> = emptyList()
) : TypedCollection {
    override fun iterator() = data.iterator()

    override val type: Type
        get() = ListType(elementType)

    val size: Int
        get() = data.size

    operator fun get(index: Int) = data[index]

}