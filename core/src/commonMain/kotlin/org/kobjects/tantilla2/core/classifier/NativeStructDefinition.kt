package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.FunctionDefinition
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.NativeFunction
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
        val function = NativeFunction(type, operation)
        definitions.add(
            FunctionDefinition(
            this,
            Definition.Kind.METHOD,
            name,
            resolvedType = type,
            resolvedValue = function,
            docString = docString
        )
        )
    }


    fun defineNativeProperty(
        name: String,
        docString: String,
        type: Type,
        getter: (RuntimeContext) -> Any?,
        setter: ((RuntimeContext) -> Any?)? = null
    ) {
        val getterType = object : FunctionType {
            override val returnType = type
            override val parameters = listOf(Parameter("self", this@NativeStructDefinition))
        }
        definitions.add(
            FunctionDefinition(
            this,
            Definition.Kind.METHOD,
            name,
            resolvedType = getterType,
            resolvedValue = NativeFunction(getterType, getter),
            docString = docString
        )
        )
        if (setter != null) {
            val setterType = object : FunctionType {
                override val returnType = Void
                override val parameters = listOf(Parameter("self", this@NativeStructDefinition), Parameter("value", type))
            }
            definitions.add(
                FunctionDefinition(
                this,
                Definition.Kind.METHOD,
                "set_$name",
                resolvedType = setterType,
                resolvedValue = NativeFunction(setterType, setter),
                docString = docString
            )
            )
        }
    }


}