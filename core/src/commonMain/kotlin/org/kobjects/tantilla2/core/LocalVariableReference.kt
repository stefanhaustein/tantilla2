package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Type

class LocalVariableReference(
    val name: String,
    override val type: Type,
    val index: Int,
    val mutable: Boolean
) : Evaluable<RuntimeContext> {
    override fun children(): List<Evaluable<RuntimeContext>> = emptyList()

    override fun eval(ctx: RuntimeContext): Any? = ctx.variables[index]

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>): Evaluable<RuntimeContext> =
        this

    override fun toString(): String = name
}