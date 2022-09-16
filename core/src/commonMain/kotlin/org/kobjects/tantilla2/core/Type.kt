package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.builtin.RootScope

interface Type {

    fun isAssignableFrom(type: Type) = type == this

    // Set scope to null to get an abbreviated type; use RootScope for fully qualified names.
    fun serializeType(writer: CodeWriter, scope: Scope?)

    val typeName: String
        get() = CodeWriter().appendType(this, RootScope).toString()

    fun resolve(name: String): Definition? = throw UnsupportedOperationException("Can't resolve '$name' for '$typeName'")
}