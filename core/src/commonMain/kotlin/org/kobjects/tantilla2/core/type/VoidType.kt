package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.definition.Scope

object VoidType : Type {
    override fun serializeType(writer: CodeWriter, scope: Scope?) {
        writer.append("Void")
    }

    object None : Typed {
        override val type: Type
            get() = VoidType

        override fun toString() = "None"
    }

    override fun toString() = "NoneType"
}