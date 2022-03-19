package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Type

class LambdaImpl(
    override val parameters: List<Parameter>,
    override val returnType: Type,
    val body: (RuntimeContext) -> Any?,
) : Lambda {

    override val name: String
        get() = "($parameters) -> $returnType"

    override fun eval(context: RuntimeContext) = body(context)

    override fun toString() = "($parameters) -> $returnType:\n  $body"
}