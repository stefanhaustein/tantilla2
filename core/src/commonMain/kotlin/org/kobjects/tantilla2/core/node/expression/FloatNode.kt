package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.type.BoolType
import org.kobjects.tantilla2.core.type.FloatType
import org.kobjects.tantilla2.core.node.Evaluable
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
    ): Node() {
        override val returnType: Type
            get() = FloatType

        override fun eval(context: LocalRuntimeContext) = value

        override fun evalF64(context: LocalRuntimeContext) = value

        override fun children() = listOf<Evaluable>()

        override fun reconstruct(newChildren: List<Evaluable>) = this

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.append(value.toString())
        }

    }

    open class Binary(
        private val name: String,
        private val precedence: Int,
        private val left: Evaluable,
        private val right: Evaluable,
        private val op: (Double, Double) -> Double
    ) : Node() {

        override val returnType: Type
            get() = FloatType

        override fun eval(context: LocalRuntimeContext): Double =
            op(left.evalF64(context), right.evalF64(context))

        override fun evalF64(context: LocalRuntimeContext): Double =
            op(left.evalF64(context), right.evalF64(context))

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Evaluable>): Evaluable =
            Binary(name, precedence, newChildren[0], newChildren[1], op)

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.appendInfix(this, parentPrecedence, name, precedence)
        }
    }

    class Add(left: Evaluable, right: Evaluable) :
        Binary("+", Precedence.PLUSMINUS, left, right, { l, r -> l + r })

    class Sub(left: Evaluable, right: Evaluable) :
        Binary("-", Precedence.PLUSMINUS, left, right, { l, r -> l - r })

    class Mul(left: Evaluable, right: Evaluable) :
        Binary("*", Precedence.MULDIV, left, right, { l, r -> l * r })

    class Div(left: Evaluable, right: Evaluable) :
        Binary("/", Precedence.MULDIV, left, right, { l, r -> l / r })

    class Mod(left: Evaluable, right: Evaluable) :
        Binary("%", Precedence.MULDIV, left, right, { l, r -> l % r })

    class Pow(left: Evaluable, right: Evaluable) :
        Binary("**", Precedence.POW, left, right, { l, r -> l.pow(r) })

    open class Unary(
        private val name: String,
        private val precedence: Int,
        private val arg: Evaluable,
        private val op: (Double) -> Double
    ) : Node() {

        override val returnType: Type
            get() = FloatType

        override fun eval(context: LocalRuntimeContext): Double =
            op(arg.evalF64(context))

        override fun evalF64(context: LocalRuntimeContext): Double =
            op(arg.evalF64(context))

        override fun children() = listOf(arg)

        override fun reconstruct(newChildren: List<Evaluable>): Evaluable = Unary(name, precedence, newChildren[0], op)

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

    class Ln(arg: Evaluable) : Unary("ln", 0, arg, { ln(it) })
    class Exp(arg: Evaluable) : Unary("exp", 0, arg, { exp(it) })
    class Neg(arg: Evaluable) : Unary("-", Precedence.UNARY, arg, { -it })


    class Eq(
        val left: Evaluable,
        val right: Evaluable,
    ): Node() {
        override val returnType: Type
            get() = BoolType

        override fun eval(context: LocalRuntimeContext): Boolean {
            return (left.evalF64(context) == right.evalF64(context))
        }

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Evaluable>): Evaluable =
            Eq(newChildren[0], newChildren[1])

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
           writer.appendInfix(this, parentPrecedence, "==", Precedence.RELATIONAL)
        }
    }

    class Ne(
        val left: Evaluable,
        val right: Evaluable,
    ): Node() {
        override val returnType: Type
            get() = BoolType

        override fun eval(context: LocalRuntimeContext): Boolean {
            return (left.evalF64(context) == right.evalF64(context))
        }

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Evaluable>): Evaluable =
            Ne(newChildren[0], newChildren[1])

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.appendInfix(this, parentPrecedence, "!=", Precedence.RELATIONAL)
        }
    }

    open class Cmp(
        val name: String,
        val left: Evaluable,
        val right: Evaluable,
        val op: (Double, Double) -> Boolean,
    ) : Node() {
        override val returnType: Type
            get() = BoolType

        override fun eval(env: LocalRuntimeContext): Boolean =
            op(left.evalF64(env), right.evalF64(env))

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Evaluable>): Evaluable =
            Cmp(name, newChildren[0], newChildren[1], op)

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.appendInfix(this, parentPrecedence, name, Precedence.RELATIONAL)
        }
    }

    class Ge(left: Evaluable, right: Evaluable) :
        Cmp(">=", left, right, { l, r -> l >= r })

    class Gt(left: Evaluable, right: Evaluable) :
        Cmp(">", left, right, { l, r -> l > r })

    class Le(left: Evaluable, right: Evaluable) :
        Cmp("<=", left, right, { l, r -> l <= r })

    class Lt(left: Evaluable, right: Evaluable) :
        Cmp("<", left, right, { l, r -> l < r })

}