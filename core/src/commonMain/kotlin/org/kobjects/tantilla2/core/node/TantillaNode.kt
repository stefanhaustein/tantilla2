package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.SerializableCode
import org.kobjects.tantilla2.core.Type

interface TantillaNode : Evaluable<RuntimeContext>, SerializableCode {

    val returnType: Type

    override fun evalF64(context: RuntimeContext): Double {
        val result = eval(context) as Number
    /*    if (result !is Double) {
            println("Double expected for $this")
        } */
        return result.toDouble()
    }
}