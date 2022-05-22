package org.kobjects.tantilla2.core

open class MetaType(open val wrapped: Type) : Type {
    override fun serializeType(writer: CodeWriter) {
        writer.append("Meta<$wrapped>")
    }

    override fun toString(): String = CodeWriter().appendType(this).toString()
}