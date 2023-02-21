package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.definition.UserRootScope

class GenericTypeMap(val fallback: Type? = null) {

    private val map = mutableMapOf<TypeVariable, Entry>()

    operator fun get(type: Type): Entry? = map[type]

    fun put(type: Type, resolved: Type, rootLevel: Boolean = false) {
        map.put(type as TypeVariable, Entry(resolved, rootLevel))
    }

    fun resolve(type: Type): Type {
        if (type !is TypeVariable) {
            throw IllegalArgumentException("Type variable expected for resolve; got: $type")
        }
        val result = map[type]
        if (result == null && fallback == null) {
            throw IllegalArgumentException("Unable to resolve type variable $type. Current mapping: $map")
        }
        return result?.type ?: fallback!!
    }

    data class Entry(
        val type: Type,
        var rootLevel: Boolean
    )

}