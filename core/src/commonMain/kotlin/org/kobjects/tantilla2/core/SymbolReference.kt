package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.RuntimeContext


data class SymbolReference(
    val name: String,
    override val type: Type,
    val value: Any?) : Evaluable<RuntimeContext> {
    override fun children() = emptyList<Evaluable<RuntimeContext>>()

    override fun eval(ctx: RuntimeContext): Any? = value

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) = this

    override fun toString() = name
}