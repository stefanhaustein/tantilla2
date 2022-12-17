package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.Evaluable
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.Typed

interface Callable : Typed, Evaluable {
    override val type: FunctionType
    override fun eval(context: LocalRuntimeContext): Any
    override val returnType: Type
        get() = type.returnType
    val scopeSize: Int
        get() = type.parameters.size
    val closure: LocalRuntimeContext?
        get() = null
}