package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.NativeFunctionDefinition
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.runtime.Void

open class NativeStructDefinition(
    parent: Scope,
    name: String,
    val ctorParams: List<Parameter> = emptyList(),
    val ctor: (RuntimeContext) -> Any? = { throw UnsupportedOperationException() },
    docString: String = "",
) : StructDefinition(parent, name, docString) {

    // TODO: May lead to nondeterminism, remove
    init {
        parent.definitions.add(this)
    }


    override val type: FunctionType
        get() = StructMetaType(this, ctorParams)

    override fun eval(context: RuntimeContext) = ctor(context)


    fun defineMethod(
        name: String,
        docString: String,
        returnType: Type,
        vararg parameter: Parameter,
        operation: (RuntimeContext) -> Any?) {
        val type = FunctionType.Impl(returnType, listOf(Parameter("self", this)) + parameter.toList())
        definitions.add(
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
        definitions.add(
            NativePropertyDefinition(
               this,
                Definition.Kind.PROPERTY,
                name,
                docString = docString,
                type = type,
                getter = getter,
                setter = setter))
    }


}