package org.kobjects.tantilla2.core.builtin

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.Type

object VoidType : Type {
    override fun serializeType(writer: CodeWriter, scope: Scope) {
        writer.append("Void")
    }
}