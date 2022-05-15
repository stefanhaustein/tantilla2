package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.F64
import org.kobjects.greenspun.core.Str
import org.kobjects.greenspun.core.Type

interface SerializableType : Type {
    fun serializeType(writer: CodeWriter)
}


fun Type.serializeType(writer: CodeWriter) {
    when (this) {
        is SerializableType -> serializeType(writer)
        F64 -> writer.append("float")
        Str -> writer.append("str")
        else -> writer.append(toString())
    }
}


val Type.typeName: String
    get() {
        val writer = CodeWriter()
        serializeType(writer)
        return writer.toString()
    }


