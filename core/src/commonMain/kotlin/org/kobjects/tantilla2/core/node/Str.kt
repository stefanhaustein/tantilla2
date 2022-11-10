package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.LocalRuntimeContext


object Str {

    class Const(
        val value: String
    ): Evaluable {
        override fun eval(ctx: LocalRuntimeContext) = value

        override fun children() = listOf<Evaluable>()

        override fun reconstruct(newChildren: List<Evaluable>) = this

        override fun toString() = "\"$value\""
    }

    class Add(
        private val left: Evaluable,
        private val right: Evaluable,
    ) : Evaluable {
        override fun eval(ctx: LocalRuntimeContext) = left.eval(ctx).toString() + right.eval(ctx).toString()

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Evaluable>) =
            Add(newChildren[0], newChildren[1])

        override fun toString() = "(+ $left $right)"
    }

    override fun toString() = "Str"
}