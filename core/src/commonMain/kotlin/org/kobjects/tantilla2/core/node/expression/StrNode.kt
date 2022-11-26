package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.Precedence
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.StrType
import org.kobjects.tantilla2.core.node.LeafNode
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.BoolType


object StrNode {

    class Const(
        val value: String
    ): LeafNode() {
        override val returnType: Type
            get() = StrType

        override fun eval(ctx: LocalRuntimeContext) = value

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.appendQuoted(value)
        }
    }

    class Add(
        private val left: Node,
        private val right: Node,
    ) : Node() {
        override val returnType: Type
            get() = StrType

        override fun eval(ctx: LocalRuntimeContext) = left.eval(ctx).toString() + right.eval(ctx).toString()

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Node>) =
            Add(newChildren[0], newChildren[1])

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.appendInfix(this, parentPrecedence, "+", Precedence.PLUSMINUS)
        }
    }

    class Eq(
        val left: Node,
        val right: Node,
    ): Node() {
        override val returnType: Type
            get() = BoolType

        override fun eval(context: LocalRuntimeContext): Boolean {
            return (left.eval(context) == right.eval(context))
        }

        override fun children() = listOf(left, right)
        override fun reconstruct(newChildren: List<Node>): Node =
            Eq(newChildren[0], newChildren[1])

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.appendInfix(this, parentPrecedence, "==", Precedence.RELATIONAL)
        }
    }

    class Ne(
        val left: Node,
        val right: Node,
    ): Node() {
        override val returnType: Type
            get() = BoolType

        override fun eval(context: LocalRuntimeContext): Boolean {
            return (left.eval(context) != right.eval(context))
        }

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Node>): Node =
            Ne(newChildren[0], newChildren[1])

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.appendInfix(this, parentPrecedence, "!=", Precedence.RELATIONAL)
        }
    }


    override fun toString() = "Str"
}