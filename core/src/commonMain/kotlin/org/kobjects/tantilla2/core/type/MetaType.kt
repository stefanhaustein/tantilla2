package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.definition.Scope

open class MetaType(open val wrapped: Type) : Type {
    override fun serializeType(writer: CodeWriter, scope: Scope?) {
        writer.append("Meta<$wrapped>")
    }

    override fun toString(): String = CodeWriter().appendType(this, null).toString()
}