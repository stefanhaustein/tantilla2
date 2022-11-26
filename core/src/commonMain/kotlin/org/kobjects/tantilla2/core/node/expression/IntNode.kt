package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.Precedence
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.BoolType
import org.kobjects.tantilla2.core.type.IntType
import org.kobjects.tantilla2.core.node.LeafNode
import org.kobjects.tantilla2.core.node.Node

/**
 * Operations.
 */
object IntNode {

    class Const(
        val value: Long
    ): LeafNode() {
        override val returnType: Type
            get() = IntType

        override fun eval(ctx: LocalRuntimeContext) = value

        override fun evalI64(ctx: LocalRuntimeContext) = value

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.append(value.toString())
        }
    }

    open class Binary(
        private val name: String,
        private val precedence: Int,
        private val left: Node,
        private val right: Node,
        private val op: (Long, Long) -> Long
    ) : Node() {
        override val returnType: Type
            get() = IntType

        override fun eval(context: LocalRuntimeContext): Long =
            op(left.evalI64(context), right.evalI64(context))

        override fun evalI64(context: LocalRuntimeContext): Long =
            op(left.evalI64(context), right.evalI64(context))

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Node>): Node =
            Binary(name, precedence, newChildren[0], newChildren[1], op)

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.appendInfix(this, parentPrecedence, name, precedence)
        }
    }

    class Add(left: Node, right: Node) :
        Binary("+", Precedence.PLUSMINUS, left, right, { l, r -> l + r })

    class Sub(left: Node, right: Node) :
        Binary("-", Precedence.PLUSMINUS, left, right, { l, r -> l - r })

    class Mul(left: Node, right: Node) :
        Binary("*", Precedence.MULDIV, left, right, { l, r -> l * r })

    class Div(left: Node, right: Node) :
        Binary("//", Precedence.MULDIV, left, right, { l, r -> l / r })

    class Mod(left: Node, right: Node) :
        Binary("%", Precedence.MULDIV, left, right, { l, r -> l % r })

    class Shl(left: Node, right: Node) :
        Binary("<<", Precedence.BITWISE_SHIFT, left, right, { l, r -> l shl r.toInt() })

    class Shr(left: Node, right: Node) :
        Binary(">>", Precedence.BITWISE_SHIFT, left, right, { l, r -> l shr r.toInt() })

    class And(left: Node, right: Node) :
        Binary("&", Precedence.BITWISE_AND, left, right, { l, r -> l and r })

    class Or(left: Node, right: Node) :
        Binary("|", Precedence.BITWISE_OR, left, right, { l, r -> l or r })

    class Xor(left: Node, right: Node) :
        Binary("^", Precedence.BITWISE_XOR, left, right, { l, r -> l xor r })

    open class Unary(
        private val name: String,
        private val precedence: Int,
        private val arg: Node,
        private val op: (Long) -> Long
    ) : Node() {
        override val returnType: Type
            get() = IntType

        override fun eval(ctx: LocalRuntimeContext): Long =
            op(arg.evalI64(ctx))

        override fun evalI64(ctx: LocalRuntimeContext): Long =
            op(arg.evalI64(ctx))

        override fun children() = listOf(arg)

        override fun reconstruct(newChildren: List<Node>): Node = Unary(name, precedence, newChildren[0], op)

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            if (precedence == 0) {
                writer.append(name)
                writer.append('(')
                writer.appendCode(children()[0])
                writer.append(')')
            } else {
                writer.appendPrefix(this, parentPrecedence, name, precedence)
            }
        }
    }
    class Neg(arg: Node) : Unary("-", Precedence.UNARY, arg, { -it })
    class Not(arg: Node) : Unary("~", Precedence.UNARY, arg, { it.inv() })

    class Eq(
        val left: Node,
        val right: Node,
    ): Node() {
        override val returnType: Type
            get() = BoolType

        override fun eval(context: LocalRuntimeContext): Boolean {
            return (left.evalI64(context) == right.evalI64(context))
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
            return (left.evalI64(context) != right.evalI64(context))
        }

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Node>): Node =
            Ne(newChildren[0], newChildren[1])

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.appendInfix(this, parentPrecedence, "!=", Precedence.RELATIONAL)
        }
    }

    open class Cmp(
        val name: String,
        val left: Node,
        val right: Node,
        val op: (Long, Long) -> Boolean,
    ) : Node() {
        override val returnType: Type
            get() = BoolType

        override fun eval(env: LocalRuntimeContext): Boolean =
            op(left.evalI64(env), right.evalI64(env))

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Node>): Node =
            Cmp(name, newChildren[0], newChildren[1], op)

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.appendInfix(this, parentPrecedence, name, Precedence.RELATIONAL)
        }
    }

    class Ge(left: Node, right: Node) :
        Cmp(">=", left, right, { left, right -> left >= right })

    class Gt(left: Node, right: Node) :
        Cmp(">", left, right, { left, right -> left > right })

    class Le(left: Node, right: Node) :
        Cmp("<=", left, right, { left, right -> left <= right })

    class Lt(left: Node, right: Node) :
        Cmp("<", left, right, { left, right -> left < right })

}