package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.NativeFunctionDefinition
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.type.Type

open class NativeTypeDefinition(
    override val parentScope: Scope?,
    override val name: String,
    override var docString: String = "",
) : Classifier() {
    override val kind: Definition.Kind
        get() = Definition.Kind.TYPE


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


    fun defineNativeProperty(
        name: String,
        docString: String,
        type: Type,
        getter: (Any?) -> Any,
        setter: ((Any?, Any) -> Unit)? = null
    ) {
        add(
            NativePropertyDefinition(
                this,
                Definition.Kind.PROPERTY,
                name,
                docString = docString,
                type = type,
                getter = getter,
                setter = setter
            ))
    }


}