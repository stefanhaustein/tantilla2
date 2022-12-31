package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.definition.Definition

interface Type {

    fun isAssignableFrom(type: Type) = type == this

    // Set scope to null to get an abbreviated type; use RootScope for fully qualified names.
    fun serializeType(writer: CodeWriter)

    // Something that can easily be used in toString()
    val typeName: String
        get() = CodeWriter().appendType(this).toString()

    fun resolve(name: String): Definition? = throw UnsupportedOperationException("Can't resolve '$name' for '$typeName'")

    val genericParameterTypes: List<Type>
        get() = emptyList()

    fun withGenericsResolved(types: List<Type>): Type =
        throw UnsupportedOperationException()
}