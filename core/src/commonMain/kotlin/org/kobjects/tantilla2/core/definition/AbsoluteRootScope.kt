package org.kobjects.tantilla2.core.definition

object AbsoluteRootScope : Scope() {
    override val parentScope: Scope?
        get() = null
    override val kind: Definition.Kind
        get() = Definition.Kind.UNIT
    override val name: String
        get() = "Absolute Root Scope"
}