package org.kobjects.tantilla2.core.builtin

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.Type


object RangeType : Type {
    override fun serializeType(writer: CodeWriter, scope: Scope) {
        writer.append("Range")
    }

    override fun toString() = "Range"
}