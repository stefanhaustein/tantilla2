package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.definition.UserRootScope

class TypeParameter(val name: String): Type {
    override fun serializeType(writer: CodeWriter) {
        writer.append(name)
    }

    override fun toString() = name



    override fun resolveGenerics(
        actualType: Type?,
        map: GenericTypeMap,
        allowNoneMatch: Boolean,
        allowAs: UserRootScope?,  // If present (!= null), allow a trait impl to fullfill the type match.
    ): Type {
        val resolved = map[this]
        if (resolved == null) {
            if (map.fallback != null) {
                return map.fallback
            }
            if (actualType == null || actualType is TypeParameter || actualType is TypeVariable) {
                throw IllegalStateException("Unable to resolve type variable $name")
            }
            map.put(this, actualType, allowNoneMatch)
            return actualType
        }
        if (actualType != null && resolved.type != actualType) {
            if (allowNoneMatch && resolved.rootLevel) {
                map.put(this, NoneType, true)
                return NoneType
            }
            throw IllegalArgumentException("Conflicting resolution for $this: Current: ${resolved.type}; expected: $actualType ")
        }
        return resolved.type

//        throw UnsupportedOperationException("Trying to resolve $this actualType: $actualType map: $map")
    }

}