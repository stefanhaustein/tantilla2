package org.kobjects.tantilla2.core.runtime

import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.Typed

class TypedList(
    override val type: ListType,
    val data: MutableList<Any?> = mutableListOf<Any?>()
) : AbstractList<Any?>(), Typed {
    override val size: Int
        get() = data.size

    override fun get(index: Int) = data[index]
}