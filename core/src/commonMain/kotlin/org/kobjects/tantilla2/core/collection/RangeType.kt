package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.type.IntType
import org.kobjects.tantilla2.core.type.Type


object RangeType : CollectionType {
    override val genericParameterTypes: List<Type>
        get() = listOf(IntType)

    override fun serializeType(writer: CodeWriter) {
        writer.append("Range")
    }

    override fun toString() = "Range"
}