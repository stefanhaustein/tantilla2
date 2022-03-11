package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable

class LambdaImpl(
    type: FunctionType,
    body: Evaluable<RuntimeContext>,
) : Lambda(type, {body.eval(it)}) {
    val body = body

    override fun toString() = "$type:\n  ${Serializer.serialize(body, "  ")}"
}