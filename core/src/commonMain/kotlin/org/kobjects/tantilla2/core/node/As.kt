package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.classifier.Adapter
import org.kobjects.tantilla2.core.classifier.ImplDefinition

class As(
    val base: Evaluable<RuntimeContext>,
    val impl: ImplDefinition,
) : Evaluable<RuntimeContext> {
    override val type: Type
        get() = impl.trait

    override fun children() = listOf(base)

    override fun eval(ctx: RuntimeContext) =
        Adapter(impl.vmt, base.eval(ctx) as RuntimeContext)

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) = As(newChildren[0], impl)
}