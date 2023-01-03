package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.CodeWriter

object UnresolvedType : Type {
    override fun serializeType(writer: CodeWriter) {
        writer.append("<Unresolved Type>")
    }
}