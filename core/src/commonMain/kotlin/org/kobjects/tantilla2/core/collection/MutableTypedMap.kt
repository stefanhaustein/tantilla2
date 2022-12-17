package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.Typed
import toLiteral

class MutableTypedMap(
    keyType: Type,
    valueType: Type,
    override val data: MutableMap<Any, Any> = mutableMapOf()
) : TypedMap(keyType, valueType, data), Typed {

    override val type: Type
        get() = MutableMapType(keyType, valueType)

    override fun toString() = data.entries.joinToString(", ", "MutableMap(", ")") { it.key.toLiteral() + ": " + it.value.toLiteral() }
}


