package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope

class ScopeType(val scope: Scope) : Type {
    override fun serializeType(writer: CodeWriter) {
        writer.append(this.scope.name)
    }

    override fun resolve(name: String): Definition? = scope.resolveStatic(name, false)

    override fun toString(): String {
        return typeName
    }
}