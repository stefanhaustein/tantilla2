package org.kobjects.tantilla2.core

class ScopeType(val scope: Scope) : Type {
    override fun serializeType(writer: CodeWriter) {
        writer.append(scope.name)
    }

    override fun resolve(name: String): Definition? = scope.resolveStatic(name, false)

}