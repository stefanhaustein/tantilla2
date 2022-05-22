package org.kobjects.tantilla2.core

object AnyType : Type {
    override fun toString() = "Any"

    override fun isAssignableFrom(type: Type) = true
    override fun serializeType(writer: CodeWriter) {
        writer.append("Any")
    }
}