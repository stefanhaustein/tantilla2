package org.kobjects.tantilla2.core

open class UnitScope(parent: Scope, val name: String) : Scope(parent), Typed {
    override val title: String
        get() = name


    override val type = ScopeType(this)
}