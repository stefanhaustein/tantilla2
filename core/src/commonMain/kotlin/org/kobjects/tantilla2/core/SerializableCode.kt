package org.kobjects.tantilla2.core

interface SerializableCode {
    fun serializeCode(writer: CodeWriter, parentPrecedence: Int = 0)
}

fun Any?.serializeCode(indent: String = "") = CodeWriter(indent).appendCode(this).toString()

