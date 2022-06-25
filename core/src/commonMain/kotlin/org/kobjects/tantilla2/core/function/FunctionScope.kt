package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.Scope

class FunctionScope(parentScope: Scope, val functionType: FunctionType) : Scope(parentScope) {

    override val title: String
        get() = "FunctionScope"


    override val supportsMethods: Boolean
        get() = true

    override val supportsLocalVariables: Boolean
        get() = true

}