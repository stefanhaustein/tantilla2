package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Type

class Self(val parsingContext: ParsingContext) : Evaluable<RuntimeContext> {
    override val type: Type
        get() = parsingContext

    override fun children(): List<Evaluable<RuntimeContext>> = listOf()

    override fun eval(ctx: RuntimeContext) = ctx

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) = this
}