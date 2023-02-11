package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.NativeFunctionDefinition
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.type.Type

open class NativeImplDefinition(
    parentScope: Scope,
    override val trait: TraitDefinition,
    override val scope: Scope,
    docString: String,
) : ImplDefinition(parentScope, docString) {
    override val name: String
        get() = "${trait.name} for ${scope.name}"


    fun defineMethod(
        name: String,
        docString: String,
        returnType: Type,
        vararg parameter: Parameter,
        operation: (LocalRuntimeContext) -> Any) {
        val type = FunctionType.Impl(returnType, listOf(Parameter("self", this)) + parameter.toList())
        add(
            NativeFunctionDefinition(
                this,
                Definition.Kind.METHOD,
                name,
                docString = docString,
                type,
                operation
            )
        )
    }
}