package org.kobjects.tantilla2.core

open class UnitScope(
    parent: Scope,
    override val name: String,
) : Scope(parent), Typed {
    override val type = ScopeType(this)
}