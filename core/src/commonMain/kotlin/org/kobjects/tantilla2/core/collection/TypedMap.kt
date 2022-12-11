package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.Typed

open class TypedMap(
    val keyType: Type,
    val valueType: Type,
    open val data: Map<Any, Any> = mutableMapOf()
) : TypedCollection {
    override fun iterator() = data.keys.iterator()

    override val type: Type
        get() = MapType(keyType, valueType)

    val size: Int
        get() = data.size

    operator fun get(key: Any): Any = data[key] ?: throw IllegalArgumentException("Key '$key' not found. in $data")
}

