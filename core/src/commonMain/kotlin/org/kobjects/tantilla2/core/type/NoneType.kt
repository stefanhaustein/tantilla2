package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.CodeWriter

object NoneType : Type {
    override fun serializeType(writer: CodeWriter) {
        writer.append("None")
    }

    object None : Typed {
        override val type: Type
            get() = NoneType

        override fun toString() = "None"
    }

    override fun toString() = "NoneType"
}