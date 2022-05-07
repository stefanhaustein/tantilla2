package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Type
import org.kobjects.greenspun.core.Void
import org.kobjects.tantilla2.core.RuntimeContext

class Assignment(
    val target: Assignable,
    val source: Evaluable<RuntimeContext>
) : Evaluable<RuntimeContext> {

    override val type: Type
        get() = Void

    override fun children() = listOf(target, source)

    override fun eval(ctx: RuntimeContext) = target.assign(ctx, source.eval(ctx))

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) =
        Assignment(newChildren[0] as Assignable, newChildren[1])
}