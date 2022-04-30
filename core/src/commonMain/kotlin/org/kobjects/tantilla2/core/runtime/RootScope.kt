package org.kobjects.tantilla2.core.runtime

import org.kobjects.greenspun.core.F64
import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.Definition
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.Scope
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.NativeFunction
import org.kobjects.tantilla2.core.function.Parameter
import kotlin.math.sqrt

class RootScope : Scope(null) {

    fun defineNative(
        name: String,
        returnType: Type,
        vararg parameter: Parameter,
        operation: (RuntimeContext) -> Any?) {
        val type = FunctionType(returnType, parameter.toList())
        val function = NativeFunction(type, operation)
        definitions[name] = Definition(
            this,
            name,
            Definition.Kind.FUNCTION,
            explicitType = type,
            explicitValue = function,
            builtin = true)
    }

    override val title: String
        get() = "Root Scope"

    init {
        defineNative(
            "sqrt", F64, Parameter("x", F64)
            ) { sqrt(it.variables[0] as Double ) }

        defineNative(
            "range", F64, Parameter("start", F64), Parameter("end", F64)
        ) { Range(it.variables[0] as Double, it.variables[1] as Double ) }

        defineNative(
            "hsl", F64, Parameter("h", F64), Parameter("s", F64), Parameter("l", F64)
        ) { hsl(it.variables[0] as Double, it.variables[1] as Double, it.variables[2] as Double).toDouble() }
    }



}