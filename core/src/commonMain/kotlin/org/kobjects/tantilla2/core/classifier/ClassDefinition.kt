package org.kobjects.tantilla2.core.classifier

import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.Typed
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Lambda

class ClassDefinition(
    val name: String,
    parentScope: Scope,
) : Scope(parentScope), Type, Typed, Lambda {
    override val type: FunctionType
        get() = ClassMetaType(this)

    override val title: String
        get() = name

    override fun eval(context: RuntimeContext) = context
}