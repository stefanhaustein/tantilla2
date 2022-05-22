package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Type

data class Parameter(
    val name: String,
    val type: Type,
) {

    fun serialize(writer: CodeWriter) {
        if (name == "self") {
            writer.append(name)
        } else {
            writer.append(name)
            writer.append(": ")
            writer.appendType(type)
        }
    }

    override fun toString(): String {
        val writer = CodeWriter()
        serialize(writer)
        return writer.toString()
    }
}