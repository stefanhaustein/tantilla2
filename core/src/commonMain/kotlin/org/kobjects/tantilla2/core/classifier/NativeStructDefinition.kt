package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.NativeFunctionDefinition
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.type.Type

open class NativeStructDefinition(
    parentScope: Scope?,
    name: String,
    docString: String = "",
    val ctorParams: List<Parameter> = emptyList(),
    val ctor: ((LocalRuntimeContext) -> Any),
) : NativeTypeDefinition(parentScope, name, docString), Callable {
    override val kind: Definition.Kind
        get() = Definition.Kind.STRUCT

    override val type: InstantiableMetaType
        get() = InstantiableMetaType(this, ctorParams)

    override fun eval(context: LocalRuntimeContext) = ctor(context)


}