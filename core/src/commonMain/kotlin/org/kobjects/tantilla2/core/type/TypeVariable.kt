package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.CodeWriter

class TypeVariable(val name: String): Type {
    override fun serializeType(writer: CodeWriter) {
        writer.append(name)
    }

    override fun toString() = name
}