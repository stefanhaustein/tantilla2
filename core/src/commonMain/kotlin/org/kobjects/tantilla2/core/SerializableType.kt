package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.F64
import org.kobjects.greenspun.core.Str
import org.kobjects.greenspun.core.Type

interface SerializableType : Type {
    fun serializeType(writer: CodeWriter)
}


val Type.typeName: String
    get() = CodeWriter().appendType(this).toString()


