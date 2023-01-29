package org.kobjects.tantilla2.core.type

class GenericTypeMap {

    val map = mutableMapOf<TypeVariable, Entry>()


    data class Entry(
        val type: Type,
        var rootLevel: Boolean
    )

}