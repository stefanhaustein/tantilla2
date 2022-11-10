package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.SerializableCode
import org.kobjects.tantilla2.core.Type


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

    val precedence: Int
        get() = 0

    val returnType: Type

    /*
    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        if (parentPrecedence > precedence) {
            writer.append('(')
            serializeCode(writer)
            writer.append(')')
        } else {
            serializeCode(writer)
        }
    }
     */

}