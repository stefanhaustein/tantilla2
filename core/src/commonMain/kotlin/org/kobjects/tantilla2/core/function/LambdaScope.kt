package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.scope.Scope
import org.kobjects.tantilla2.core.scope.ScopeType
import org.kobjects.tantilla2.core.type.Type

class LambdaScope(
    override val parentScope: Scope?
) : Scope() {
    override val kind
        get() = Definition.Kind.FUNCTION

    override val name
        get() = "<Anonymous>"

    override val supportsLocalVariables: Boolean
        get() = true

    override val type: Type
        get() = ScopeType(this)
}