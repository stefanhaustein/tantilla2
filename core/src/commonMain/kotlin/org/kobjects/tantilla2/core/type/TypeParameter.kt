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
        throw UnsupportedOperationException()
    }

}