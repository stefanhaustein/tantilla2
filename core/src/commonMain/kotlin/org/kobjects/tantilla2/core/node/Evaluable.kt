package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.LocalRuntimeContext


interface Evaluable {
    fun eval(context: LocalRuntimeContext): Any?

    fun evalF64(context: LocalRuntimeContext): Double {
        return (eval(context) as Number).toDouble()
    }

    fun evalI64(context: LocalRuntimeContext): Long {
        return (eval(context) as Number).toLong()
    }

    fun children(): List<Evaluable>

    fun reconstruct(newChildren: List<Evaluable>): Evaluable
}