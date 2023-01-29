package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.CodeWriter

class TypeVariable(val name: String): Type {
    override fun serializeType(writer: CodeWriter) {
        writer.append(name)
    }

    override fun toString() = name


    override fun resolveGenerics(expectedType: Type, map: GenericTypeMap, allowNoneMatch: Boolean, allowAs: Boolean): Type {
        val resolved = map.map[this]
        if (resolved == null) {
            map.map[this] = GenericTypeMap.Entry(expectedType, allowNoneMatch)
            return expectedType
        }
        if (resolved.type != expectedType) {
            if (allowNoneMatch && resolved.rootLevel) {
                map.map[this] = GenericTypeMap.Entry(NoneType, true)
                return NoneType
            }
            throw IllegalArgumentException("Conflicting resolution for $this: Current: ${resolved.type}; expected: $expectedType ")
        }
        return expectedType
    }

}