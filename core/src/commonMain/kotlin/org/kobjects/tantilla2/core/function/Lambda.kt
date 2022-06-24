package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.Typed

interface Lambda : Typed {
    override val type: FunctionType
    fun eval(context: RuntimeContext): Any?
    val scopeSize: Int
        get() = type.parameters.size
    val closure: RuntimeContext?
        get() = null
}