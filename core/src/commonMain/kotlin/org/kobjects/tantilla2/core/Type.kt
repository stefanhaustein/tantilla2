package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.builtin.RootScope

interface Type {

    fun isAssignableFrom(type: Type) = type == this

    fun serializeType(writer: CodeWriter, scope: Scope)

    val typeName: String
        get() = CodeWriter().appendType(this, RootScope).toString()

    fun resolve(name: String): Definition? = throw UnsupportedOperationException("Can't resolve '$name' for '$typeName'")
}