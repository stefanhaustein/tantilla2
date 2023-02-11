package org.kobjects.tantilla2.core.type

class GenericTypeMap {

    val map = mutableMapOf<TypeVariable, Entry>()

    fun put(type: Type, resolved: Type, rootLevel: Boolean = false) {
        map.put(type as TypeVariable, Entry(resolved, rootLevel))
    }

    fun resolve(type: Type): Type = map[type as TypeVariable]!!.type

    data class Entry(
        val type: Type,
        var rootLevel: Boolean
    )

}