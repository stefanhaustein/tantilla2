package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Control
import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.F64
import org.kobjects.tantilla2.core.CodeWriter

interface SerializableCode {
    fun serializeCode(writer: CodeWriter, precedence: Int = 0)
}

fun Any?.serializeCode(indent: String = "") = CodeWriter(indent).appendCode(this).toString()

