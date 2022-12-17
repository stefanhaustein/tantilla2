package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.Typed
import toLiteral

class MutableTypedSet(
    elementType: Type,
    override val data: MutableSet<Any> = mutableSetOf()
) : TypedSet(elementType, data), Typed {

    override val type: Type
        get() = MutableSetType(elementType)


    override fun toString() = data.joinToString(", ", "MutableSet(", ")") { it.toLiteral() }

}