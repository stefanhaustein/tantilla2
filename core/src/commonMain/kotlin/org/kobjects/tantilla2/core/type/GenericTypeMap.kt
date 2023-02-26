package org.kobjects.tantilla2.core.type

class GenericTypeMap(val fallback: Type? = null) {

    private val map = mutableMapOf<TypeParameter, Entry>()

    operator fun get(type: Type): Entry? = map[type]

    fun put(type: Type, resolved: Type, rootLevel: Boolean = false) {
        map.put(type as TypeParameter, Entry(resolved, rootLevel))
    }

    fun resolve(type: Type): Type {
        if (type !is TypeParameter) {
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