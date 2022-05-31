package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.NativeFunction
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.runtime.RootScope

open class NativeClassDefinition(var name: String, parent: Scope = RootScope) : Scope(parent), Type {
    override val title: String
        get() = name


    override fun serializeType(writer: CodeWriter) {
        writer.append(name)
    }


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
            Definition.Kind.FUNCTION,
            name,
            resolvedType = type,
            resolvedValue = function,
            docString = docString
        ))
    }

    init {
        val def = Definition(
            parent,
            Definition.Kind.CLASS,
            name,
            resolvedType = this,
            resolvedValue = this
        )
        parent.add(def)
    }

}