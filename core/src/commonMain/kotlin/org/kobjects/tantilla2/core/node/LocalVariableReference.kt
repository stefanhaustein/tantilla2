package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.RuntimeContext

class LocalVariableReference(
    val name: String,
    override val type: Type,
    val index: Int,
    val mutable: Boolean
) : Assignable {
    override fun children(): List<Evaluable<RuntimeContext>> = emptyList()

    override fun eval(ctx: RuntimeContext): Any? = ctx.variables[index]

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>): Evaluable<RuntimeContext> =
        this

    override fun assign(context: RuntimeContext, value: Any?) {
        context.variables[index] = value
    }

    override fun toString(): String = name
}