package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope

interface Type {

    fun isAssignableFrom(type: Type) = type == this

    // Set scope to null to get an abbreviated type; use RootScope for fully qualified names.
    fun serializeType(writer: CodeWriter, scope: Scope?)

    val typeName: String
        get() = CodeWriter().appendType(this, null).toString()

    fun resolve(name: String): Definition? = throw UnsupportedOperationException("Can't resolve '$name' for '$typeName'")
}