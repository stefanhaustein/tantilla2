package org.kobjects.tantilla2.core.scope

import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.DocStringUpdatable
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.Typed

open class UnitScope(
    override val parentScope: Scope?,
    override val name: String = "",
    override var docString: String = "",
) : Scope(), Typed, DocStringUpdatable {
    override val kind: Definition.Kind
        get() = Definition.Kind.UNIT


    override val type: Type
        get() = UnitType(this)
}