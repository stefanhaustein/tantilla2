package org.kobjects.tantilla2.core.runtime

import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.UnitScope
import org.kobjects.tantilla2.core.function.Parameter

object RootScope : Scope() {

    override val parentScope: Scope?
        get() = null

    override val name: String
        get() = "Root Scope"

    init {



        add(MathScope)

    }

    override val kind: Definition.Kind
        get() = Definition.Kind.UNIT

    override var docString: String
        get() = ""
        set(_) = throw UnsupportedOperationException()

}