package org.kobjects.tantilla2.core

open class UnitScope(
    parent: Scope,
    override val name: String = "",
    override var docString: String = "",
) : Scope(parent, ""), Typed {
    override val type = ScopeType(this)
    override val kind: Definition.Kind
        get() = Definition.Kind.UNIT
}