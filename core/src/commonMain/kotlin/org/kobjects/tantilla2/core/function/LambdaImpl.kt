package org.kobjects.tantilla2.core.function

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.serialize

class LambdaImpl(
    override val type: FunctionType,
    val body: Evaluable<RuntimeContext>,
    ) : Callable {


        // get() = "(${type.parameters}) -> ${type.returnType}"

        override fun eval(context: RuntimeContext) = body.eval(context)

        override fun toString() = "$type:\n  ${body.serialize("  ")}"
    }
