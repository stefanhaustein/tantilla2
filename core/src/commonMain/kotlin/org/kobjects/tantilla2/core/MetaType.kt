package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.builtin.RootScope

open class MetaType(open val wrapped: Type) : Type {
    override fun serializeType(writer: CodeWriter, scope: Scope?) {
        writer.append("Meta<$wrapped>")
    }

    override fun toString(): String = CodeWriter().appendType(this, null).toString()
}