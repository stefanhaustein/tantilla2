package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.CodeWriter

open class MetaType(open val wrapped: Type) : Type {
    override fun serializeType(writer: CodeWriter) {
        writer.append("Meta[$wrapped]")
    }

    override fun toString(): String = CodeWriter(forTitle = true).appendType(this).toString()
}