package org.kobjects.tantilla2.classifier

import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.Typed
import org.kobjects.tantilla2.function.Callable

class ClassDefinition(
    override val name: String,
    parentScope: Scope,
) : Scope(parentScope), Type, Typed, Callable {
    override val type: Type
        get() = ClassMetaType(this)


    override fun eval(context: RuntimeContext) = context
}