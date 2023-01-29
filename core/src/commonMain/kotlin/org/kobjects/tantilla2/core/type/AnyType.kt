package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.CodeWriter

object AnyType : Type {
    override fun toString() = "Any"

    override fun isAssignableFrom(type: Type, allowAs: Boolean) = true
    override fun serializeType(writer: CodeWriter) {
        writer.append("Any")
    }
}