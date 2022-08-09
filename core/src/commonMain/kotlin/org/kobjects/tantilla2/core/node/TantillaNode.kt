package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.SerializableCode
import org.kobjects.tantilla2.core.Type

fun Evaluable<LocalRuntimeContext>.containsNode(node: Evaluable<LocalRuntimeContext>): Boolean {
    if (this == node) {
        return true
    }
    for (child in children()) {
        if (child == node) {
            return true
        }
    }
    return false
}


interface TantillaNode : Evaluable<LocalRuntimeContext>, SerializableCode {

    val returnType: Type

    override fun evalF64(context: LocalRuntimeContext): Double {
        val result = eval(context) as Number
    /*    if (result !is Double) {
            println("Double expected for $this")
        } */
        return result.toDouble()
    }
}