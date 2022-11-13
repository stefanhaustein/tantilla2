package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.definition.Scope

object AnyType : Type {
    override fun toString() = "Any"

    override fun isAssignableFrom(type: Type) = true
    override fun serializeType(writer: CodeWriter, scope: Scope?) {
        writer.append("Any")
    }
}