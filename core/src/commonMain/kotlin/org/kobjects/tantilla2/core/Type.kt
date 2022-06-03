package org.kobjects.tantilla2.core

interface Type {

    fun isAssignableFrom(type: Type) = type == this

    fun serializeType(writer: CodeWriter)

    val typeName: String
        get() = CodeWriter().appendType(this).toString()

    fun resolve(name: String): Definition = throw UnsupportedOperationException("Can't resolve '$name' for '$typeName'")
}