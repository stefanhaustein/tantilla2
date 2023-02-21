package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.CodeWriter

object PlaceHolderType : Type {
    override fun serializeType(writer: CodeWriter) {
        writer.append("?")
    }
}