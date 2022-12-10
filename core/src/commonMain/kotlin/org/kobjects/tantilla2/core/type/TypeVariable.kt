package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.definition.Scope

class TypeVariable(val name: String): Type {
    override fun serializeType(writer: CodeWriter, scope: Scope?) {
        writer.append(name)
    }

    override fun toString() = name
}