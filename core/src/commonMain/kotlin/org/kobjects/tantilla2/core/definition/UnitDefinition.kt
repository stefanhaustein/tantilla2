package org.kobjects.tantilla2.core.definition

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.type.ScopeType
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.Typed

open class UnitDefinition(
    override val parentScope: Scope?,
    override val name: String = "",
    override var docString: String = "",
) : Scope(), Typed {
    override val type = ScopeType(this)
    override val kind: Definition.Kind
        get() = Definition.Kind.UNIT
}