package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.type.BoolType
import org.kobjects.tantilla2.core.type.FloatType
import org.kobjects.tantilla2.core.node.LeafNode
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.Type
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

/**
 * Operations.
 */
object FloatNode {

    class Const(
        val value: Double
    ): LeafNode() {
        override val returnType: Type
            get() = FloatType

        override fun eval(context: LocalRuntimeContext) = value

        override fun evalF64(context: LocalRuntimeContext) = value

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.append(value.toString())
        }

    }

    open class Binary(
        private val name: String,
        private val precedence: Int,
        private val left: Node,
        private val right: Node,
        private val op: (Double, Double) -> Double
    ) : Node() {

        override val returnType: Type
            get() = FloatType

        override fun eval(context: LocalRuntimeContext): Double =
            op(left.evalF64(context), right.evalF64(context))

        override fun evalF64(context: LocalRuntimeContext): Double =
            op(left.evalF64(context), right.evalF64(context))

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
        Binary("/", Precedence.MULDIV, left, right, { l, r -> l / r })

    class Mod(left: Node, right: Node) :
        Binary("%", Precedence.MULDIV, left, right, { l, r -> l % r })

    class Pow(left: Node, right: Node) :
        Binary("**", Precedence.POW, left, right, { l, r -> l.pow(r) })

    open class Unary(
        private val name: String,
        private val precedence: Int,
        private val arg: Node,
        private val op: (Double) -> Double
    ) : Node() {

        override val returnType: Type
            get() = FloatType

        override fun eval(context: LocalRuntimeContext): Double =
            op(arg.evalF64(context))

        override fun evalF64(context: LocalRuntimeContext): Double =
            op(arg.evalF64(context))

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

    class Ln(arg: Node) : Unary("ln", 0, arg, { ln(it) })
    class Exp(arg: Node) : Unary("exp", 0, arg, { exp(it) })
    class Neg(arg: Node) : Unary("-", Precedence.UNARY, arg, { -it })


    class Eq(
        val left: Node,
        val right: Node,
    ): Node() {
        override val returnType: Type
            get() = BoolType

        override fun eval(context: LocalRuntimeContext) =
            left.evalF64(context) == right.evalF64(context)

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

        override fun eval(context: LocalRuntimeContext) =
            left.evalF64(context) != right.evalF64(context)

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
        val op: (Double, Double) -> Boolean,
    ) : Node() {
        override val returnType: Type
            get() = BoolType

        override fun eval(env: LocalRuntimeContext): Boolean =
            op(left.evalF64(env), right.evalF64(env))

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Node>): Node =
            Cmp(name, newChildren[0], newChildren[1], op)

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.appendInfix(this, parentPrecedence, name, Precedence.RELATIONAL)
        }
    }

    class Ge(left: Node, right: Node) :
        Cmp(">=", left, right, { l, r -> l >= r })

    class Gt(left: Node, right: Node) :
        Cmp(">", left, right, { l, r -> l > r })

    class Le(left: Node, right: Node) :
        Cmp("<=", left, right, { l, r -> l <= r })

    class Lt(left: Node, right: Node) :
        Cmp("<", left, right, { l, r -> l < r })

}