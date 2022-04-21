package org.kobjects.tantilla2.classifier

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.RuntimeContext

class As(
    val base: Evaluable<RuntimeContext>,
    val impl: ImplDefinition,
) : Evaluable<RuntimeContext> {
    override val type: Type
        get() = impl.trait

    override fun children() = listOf(base)

    override fun eval(ctx: RuntimeContext) = RuntimeContext(mutableListOf(
        impl.vmt, base.eval(ctx)
    ))

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) = As(newChildren[0], impl)
}