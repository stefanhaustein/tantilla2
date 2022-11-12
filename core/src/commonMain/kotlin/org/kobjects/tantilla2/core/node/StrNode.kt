package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.Precedence
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.builtin.StrType


object StrNode {

    class Const(
        val value: String
    ): Node() {
        override val returnType: Type
            get() = StrType

        override fun eval(ctx: LocalRuntimeContext) = value

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.appendTripleQuoted(value)
        }
    }

    class Add(
        private val left: Evaluable,
        private val right: Evaluable,
    ) : Node() {
        override val returnType: Type
            get() = StrType

        override fun eval(ctx: LocalRuntimeContext) = left.eval(ctx).toString() + right.eval(ctx).toString()

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Evaluable>) =
            Add(newChildren[0], newChildren[1])

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.appendInfix(this, parentPrecedence, "+", Precedence.PLUSMINUS)
        }
    }

    override fun toString() = "Str"
}