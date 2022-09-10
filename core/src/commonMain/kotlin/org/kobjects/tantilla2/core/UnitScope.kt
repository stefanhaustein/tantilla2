package org.kobjects.tantilla2.core

open class UnitScope(
    override val parentScope: Scope,
    override val name: String = "",
    override var docString: String = "",
) : Scope(), Typed, Type {
    override val type = ScopeType(this)
    override val kind: Definition.Kind
        get() = Definition.Kind.UNIT

    override fun serializeType(writer: CodeWriter, scope: Scope) {
        writer.append(name)
    }
}