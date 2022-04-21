package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable

class LambdaImpl(
    override val type: FunctionType,
    val body: Evaluable<RuntimeContext>,
    ) : Callable {


        // get() = "(${type.parameters}) -> ${type.returnType}"

        override fun eval(context: RuntimeContext) = body.eval(context)

        override fun toString() = "$type:\n  $body"
    }
