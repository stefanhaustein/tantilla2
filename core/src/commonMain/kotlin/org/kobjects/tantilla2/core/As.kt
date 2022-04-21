package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Type

class As(
    val base: Evaluable<RuntimeContext>,
    val impl: TraitImpl,
) : Evaluable<RuntimeContext> {
    override val type: Type
        get() = impl.trait

    override fun children() = listOf(base)

    override fun eval(ctx: RuntimeContext) = RuntimeContext(mutableListOf(
        impl.vmt, base.eval(ctx)
    ))

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) = As(newChildren[0], impl)
}