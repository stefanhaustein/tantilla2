package org.kobjects.tantilla2.core.classifier

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.FunctionDefinition
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.NativeFunction
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.node.NativeEvaluable
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
        val body = object : Evaluable<RuntimeContext> {
            override fun children() = emptyList<Evaluable<RuntimeContext>>()
            override fun eval(context: RuntimeContext) = operation(context)
            override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) = this
        }
        definitions.add(
            FunctionDefinition(
            this,
            Definition.Kind.METHOD,
            name,
            resolvedType = type,
            resolvedBody = body,
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
            resolvedBody = NativeEvaluable(getter),
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
                resolvedBody = NativeEvaluable(setter),
                docString = docString
            )
            )
        }
    }


}