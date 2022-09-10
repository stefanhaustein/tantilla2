package org.kobjects.tantilla2.core.builtin

import org.kobjects.tantilla2.core.AnyType
import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.Scope

object RootScope : Scope() {

    init {
        add(Bool)
        add(F64)
        add(I64)
        add(ListType(AnyType))
        add(Str)
    }


    override val parentScope: Scope?
        get() = null

    override val name: String
        get() = "Root Scope"

    override val kind: Definition.Kind
        get() = Definition.Kind.UNIT

    override var docString: String
        get() = ""
        set(_) = throw UnsupportedOperationException()

}