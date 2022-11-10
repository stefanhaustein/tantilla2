package org.kobjects.tantilla2.core

interface SerializableCode {
    fun serializeCode(writer: CodeWriter, precedence: Int = 0)
}

fun Any?.serializeCode(indent: String = "") = CodeWriter(indent).appendCode(this).toString()

