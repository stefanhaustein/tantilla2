package org.kobjects.tantilla2.core.type

class GenericTypeMap(val fallback: Type? = null) {

    private val map = mutableMapOf<Type, Entry>()
    private var varCount = 0

    operator fun get(type: Type): Entry? = map[type]

    fun put(type: TypeVariable, resolved: Type, rootLevel: Boolean = false) {
        map.put(type, Entry(resolved, rootLevel))
    }

    /*
    fun resolve(type: Type): Type {
        if (type !is TypeParameter) {
            throw IllegalArgumentException("Type variable expected for resolve; got: $type")
        }
        val result = map[type]
        if (result == null && fallback == null) {
            throw IllegalArgumentException("Unable to resolve type variable $type. Current mapping: $map")
        }
        return result?.type ?: fallback!!
    }*/

    fun map(type: Type): Type {
        var replacement = map[type]?.type
        if (replacement == null && type is TypeParameter) {
            replacement = fallback
        }
        return replacement ?: type
    }


    data class Entry(
        val type: Type,
        var rootLevel: Boolean
    )

    fun createVariable() = TypeVariable("T${varCount++}")

}