package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.F64
import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.NativeFunction
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.runtime.Range
import kotlin.math.sqrt

class RootScope : Scope(null) {

    fun defineNative(
        name: String,
        returnType: Type,
        vararg paramter: Parameter,
        operation: (RuntimeContext) -> Any?) =
        defineValue(name, NativeFunction(FunctionType(returnType, paramter.toList()), operation))

    init {
        defineNative(
            "sqrt", F64, Parameter("x", F64)
            ) { sqrt(it.variables[0] as Double ) }

        defineNative(
            "range", F64, Parameter("start", F64), Parameter("end", F64)
        ) { Range(it.variables[0] as Double, it.variables[1] as Double ) }

    }

}