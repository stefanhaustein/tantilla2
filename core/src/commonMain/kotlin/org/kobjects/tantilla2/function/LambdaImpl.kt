package org.kobjects.tantilla2.function

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.RuntimeContext

class LambdaImpl(
    override val type: FunctionType,
    val body: Evaluable<RuntimeContext>,
    ) : Callable {


        // get() = "(${type.parameters}) -> ${type.returnType}"

        override fun eval(context: RuntimeContext) = body.eval(context)

        override fun toString() = "$type:\n  $body"
    }
