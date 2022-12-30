package org.kobjects.tantilla2.core.definition

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.type.Type

class EnumDefinition(
    override val parentScope: Scope,
    override val name: String,
) : Scope(), Type {
    override val kind: Definition.Kind
        get() = Definition.Kind.ENUM

    override fun serializeType(writer: CodeWriter) {
        TODO("Not yet implemented")
    }

}