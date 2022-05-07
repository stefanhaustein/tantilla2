package org.kobjects.tantilla2.core.runtime

import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.tantillaName

class ListType(val elementType: Type) : Type {
    override fun toString() = "List[${elementType.tantillaName}]"

    fun empty() = TypedList(this)

    fun create(size: Int, init: (Int) -> Any?) = TypedList(this, MutableList(size, init))
}