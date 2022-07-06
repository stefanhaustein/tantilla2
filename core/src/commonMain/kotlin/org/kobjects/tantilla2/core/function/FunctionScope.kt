package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.Scope

class FunctionScope(
    override val parentScope: Scope,
    val functionType: FunctionType,
) : Scope() {

    override val name: String
        get() = "FunctionScope"

    override var docString: String
        get() = ""
        set(_) = throw UnsupportedOperationException()

    override val supportsMethods: Boolean
        get() = true

    override val supportsLocalVariables: Boolean
        get() = true

    override val kind: Definition.Kind
        get() = Definition.Kind.FUNCTION

}