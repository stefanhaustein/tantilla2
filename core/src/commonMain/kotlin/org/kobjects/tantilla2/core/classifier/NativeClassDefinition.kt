package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Lambda
import org.kobjects.tantilla2.core.function.NativeFunction
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.runtime.RootScope
import org.kobjects.tantilla2.core.runtime.Void

open class NativeClassDefinition(
    val name: String,
    parent: Scope = RootScope,
    val ctorParams: List<Parameter> = emptyList(),
    val ctor: ((RuntimeContext) -> Any?) = { throw UnsupportedOperationException() },
) : Scope(parent), Type, Typed, Lambda {
    override val title: String
        get() = name

    override val type: FunctionType
        get() = NativeClassMetaType(this, ctorParams)

    override fun eval(context: RuntimeContext) = ctor(context)

    override fun serializeType(writer: CodeWriter) {
        writer.append(name)
    }

    override fun resolve(name: String): Definition? = resolveDynamic(name, false)


    fun defineMethod(
        name: String,
        docString: String,
        returnType: Type,
        vararg parameter: Parameter,
        operation: (RuntimeContext) -> Any?) {
        val type = FunctionType.Impl(returnType, listOf(Parameter("self", this)) + parameter.toList())
        val function = NativeFunction(type, operation)
        add(Definition(
            this,
            Definition.Kind.METHOD,
            name,
            resolvedType = type,
            resolvedValue = function,
            docString = docString
        ))
    }

    init {
        val def = Definition(
            parent,
            Definition.Kind.STRUCT,
            name,
            resolvedValue = this
        )
        parent.add(def)
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
            override val parameters = listOf(Parameter("self", this@NativeClassDefinition))
        }
        add(Definition(
            this,
            Definition.Kind.METHOD,
            name,
            resolvedType = getterType,
            resolvedValue = NativeFunction(getterType, getter),
            docString = docString
        ))
        if (setter != null) {
            val setterType = object : FunctionType {
                override val returnType = Void
                override val parameters = listOf(Parameter("self", this@NativeClassDefinition), Parameter("value", type))
            }
            add(Definition(
                this,
                Definition.Kind.METHOD,
                "set_$name",
                resolvedType = setterType,
                resolvedValue = NativeFunction(setterType, setter),
                docString = docString
            ))
        }
    }


}