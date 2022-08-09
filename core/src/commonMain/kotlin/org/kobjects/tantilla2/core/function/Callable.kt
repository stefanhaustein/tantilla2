package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.Typed

interface Callable : Typed {
    override val type: FunctionType
    fun eval(context: LocalRuntimeContext): Any?
    val scopeSize: Int
        get() = type.parameters.size
    val closure: LocalRuntimeContext?
        get() = null
}