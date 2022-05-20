package org.kobjects.tantilla2.core



interface SerializableType : Type {
    fun serializeType(writer: CodeWriter)
}


val Type.typeName: String
    get() = CodeWriter().appendType(this).toString()


