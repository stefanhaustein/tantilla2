package org.kobjects.tantilla2.core.runtime

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Type


object RangeType : Type {
    override fun serializeType(writer: CodeWriter) {
        writer.append("Range")
    }

    override fun toString() = "Range"
}