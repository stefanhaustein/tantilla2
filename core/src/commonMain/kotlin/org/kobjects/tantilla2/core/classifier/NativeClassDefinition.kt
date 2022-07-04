package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Lambda
import org.kobjects.tantilla2.core.function.NativeFunction
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.runtime.RootScope
import org.kobjects.tantilla2.core.runtime.Void

open class NativeClassDefinition(
    override val name: String,
    parent: Scope = RootScope,
    val ctorParams: List<Parameter> = emptyList(),
    val ctor: ((RuntimeContext) -> Any?) = { throw UnsupportedOperationException() },
    override var docString: String = "",
) : Scope(parent), Type, Typed, Lambda {
    override val supportsMethods: Boolean
        get() = true

    override val supportsLocalVariables: Boolean
        get() = true

    override val kind: Definition.Kind
        get() = Definition.Kind.STRUCT

    override val type: FunctionType
        get() = NativeClassMetaType(this, ctorParams)

    override fun eval(context: RuntimeContext) = ctor(context)

    override fun serializeType(writer: CodeWriter) {
        writer.append(this.name)
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
        definitions.add(FunctionDefinition(
            this,
            Definition.Kind.METHOD,
            name,
            resolvedType = type,
            resolvedValue = function,
            docString = docString
        ))
    }

    init {
        parent.definitions.add(this)
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
        definitions.add(FunctionDefinition(
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
            definitions.add(FunctionDefinition(
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