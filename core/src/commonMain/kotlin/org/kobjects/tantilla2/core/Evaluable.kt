package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.type.Type


/**
 * Common super interface of Callable and Node
 */
interface Evaluable {
    fun eval(context: LocalRuntimeContext): Any

    fun evalF64(context: LocalRuntimeContext): Double {
        return (eval(context) as Number).toDouble()
    }

    fun evalI64(context: LocalRuntimeContext): Long {
        return (eval(context) as Number).toLong()
    }


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