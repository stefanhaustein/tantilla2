package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.Type

class LambdaScope(
    override val parentScope: Scope?
) : Scope() {
    override val kind
        get() = Definition.Kind.FUNCTION

    override val name
        get() = "<Anonymous>"

    override val supportsLocalVariables: Boolean
        get() = true
}