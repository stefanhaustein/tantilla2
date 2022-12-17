package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.Typed
import toLiteral

open class TypedSet(
    val elementType: Type,
    open val data: Set<Any> = emptySet()
) : TypedCollection {
    override fun iterator() = data.iterator()

    override val type: Type
        get() = SetType(elementType)

    val size: Int
        get() = data.size

    override fun toString() = data.joinToString(", ", "{", "}") { it.toLiteral() }

    override fun hashCode() = data.hashCode()

    override fun equals(other: Any?) =
        other is TypedSet && other.type == type && other.data == data

}