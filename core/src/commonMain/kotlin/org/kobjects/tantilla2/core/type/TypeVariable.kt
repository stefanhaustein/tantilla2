package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.CodeWriter

class TypeVariable(val name: String): Type {
    override fun serializeType(writer: CodeWriter) {
        writer.append(name)
    }

    override fun toString() = name



    override fun resolveGenerics(actualType: Type?, map: GenericTypeMap, allowNoneMatch: Boolean, allowAs: Boolean): Type {
        val resolved = map.map[this]
        if (resolved == null) {
            if (actualType == null) {
                throw IllegalStateException("Unable to resolve type variable $name")
            }
            map.map[this] = GenericTypeMap.Entry(actualType, allowNoneMatch)
            return actualType
        }
        if (actualType != null && resolved.type != actualType) {
            if (allowNoneMatch && resolved.rootLevel) {
                map.map[this] = GenericTypeMap.Entry(NoneType, true)
                return NoneType
            }
            throw IllegalArgumentException("Conflicting resolution for $this: Current: ${resolved.type}; expected: $actualType ")
        }
        return resolved.type
    }

}