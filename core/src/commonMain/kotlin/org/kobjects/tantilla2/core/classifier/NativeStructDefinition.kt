package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.NativeFunctionDefinition
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.type.Type

open class NativeStructDefinition(
    parent: Scope?,
    name: String,
    docString: String = "",
    val ctorParams: List<Parameter> = emptyList(),
    val ctor: (LocalRuntimeContext) -> Any? = { throw UnsupportedOperationException() },
) : StructDefinition(parent, name, docString) {
    override val kind: Definition.Kind
        get() = Definition.Kind.TYPE

    override val type: FunctionType
        get() = StructMetaType(this, ctorParams)

    override fun eval(context: LocalRuntimeContext) = ctor(context)


    fun defineMethod(
        name: String,
        docString: String,
        returnType: Type,
        vararg parameter: Parameter,
        operation: (LocalRuntimeContext) -> Any?) {
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
        getter: (Any?) -> Any?,
        setter: ((Any?, Any?) -> Unit)? = null
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