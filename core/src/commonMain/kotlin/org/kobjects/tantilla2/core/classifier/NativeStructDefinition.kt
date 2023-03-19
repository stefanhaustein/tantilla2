package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.scope.Scope
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.Parameter

/** Basically a native type with a constructor */
open class NativeStructDefinition(
    parentScope: Scope?,
    name: String,
    docString: String = "",
    val ctor: ((LocalRuntimeContext) -> Any),
    vararg ctorParams: Parameter,
) : NativeTypeDefinition(parentScope, name, docString), Callable {
    var ctorParams = ctorParams.toList()

    override val kind: Definition.Kind
        get() = Definition.Kind.STRUCT

    override val type: InstantiableMetaType
        get() = InstantiableMetaType(this, ctorParams)

    override fun eval(context: LocalRuntimeContext) = ctor(context)


}