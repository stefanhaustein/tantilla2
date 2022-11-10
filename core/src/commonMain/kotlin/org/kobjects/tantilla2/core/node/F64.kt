package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.builtin.BoolType
import org.kobjects.tantilla2.core.builtin.FloatType
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

/**
 * Operations.
 */
object F64 {

    class Const(
        val value: Double
    ): Evaluable {
        override val returnType: Type
            get() = FloatType

        override fun eval(ctx: LocalRuntimeContext) = value

        override fun evalF64(context: LocalRuntimeContext) = value

        override fun children() = listOf<Evaluable>()

        override fun reconstruct(newChildren: List<Evaluable>) = this

        override fun toString() = value.toString()

    }

    open class Binary(
        private val name: String,
        private val left: Evaluable,
        private val right: Evaluable,
        private val op: (Double, Double) -> Double
    ) : Evaluable {

        override val returnType: Type
            get() = FloatType

        override fun eval(context: LocalRuntimeContext): Double =
            op(left.evalF64(context), right.evalF64(context))

        override fun evalF64(context: LocalRuntimeContext): Double =
            op(left.evalF64(context), right.evalF64(context))

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Evaluable>): Evaluable =
            Binary(name, newChildren[0], newChildren[1], op)

        override fun toString() = "($name $left $right)"
    }

    class Add(left: Evaluable, right: Evaluable) :
        Binary("+", left, right, { l, r -> l + r })

    class Mod(left: Evaluable, right: Evaluable) :
        Binary("%", left, right, { l, r -> l % r })

    class Sub(left: Evaluable, right: Evaluable) :
        Binary("-", left, right, { l, r -> l - r })

    class Mul(left: Evaluable, right: Evaluable) :
        Binary("*", left, right, { l, r -> l * r })

    class Div(left: Evaluable, right: Evaluable) :
        Binary("/", left, right, { l, r -> l / r })

    class Pow(left: Evaluable, right: Evaluable) :
        Binary("**", left, right, { l, r -> l.pow(r) })

    open class Unary(
        private val name: String,
        private val arg: Evaluable,
        private val op: (Double) -> Double
    ) : Evaluable {

        override val returnType: Type
            get() = FloatType

        override fun eval(ctx: LocalRuntimeContext): Double =
            op(arg.evalF64(ctx))

        override fun evalF64(ctx: LocalRuntimeContext): Double =
            op(arg.evalF64(ctx))

        override fun children() = listOf(arg)

        override fun reconstruct(newChildren: List<Evaluable>): Evaluable = Unary(name, newChildren[0], op)

        override fun toString(): String = "($name $arg)"
    }

    class Ln(arg: Evaluable) : Unary("ln", arg, { ln(it) })
    class Exp(arg: Evaluable) : Unary("exp", arg, { exp(it) })
    class Neg(arg: Evaluable) : Unary("neg", arg, { -it })


    class Eq(
        val left: Evaluable,
        val right: Evaluable,
    ): Evaluable {
        override val returnType: Type
            get() = BoolType

        override fun eval(context: LocalRuntimeContext): Boolean {
            return (left.evalF64(context) == right.evalF64(context))
        }

        override fun children() = listOf(left, right)
        override fun reconstruct(newChildren: List<Evaluable>): Evaluable =
            Eq(newChildren[0], newChildren[1])

        override fun toString() =
            "(= $left $right)"
    }

    class Ne(
        val left: Evaluable,
        val right: Evaluable,
    ): Evaluable {
        override val returnType: Type
            get() = BoolType

        override fun eval(context: LocalRuntimeContext): Boolean {
            return (left.evalF64(context) == right.evalF64(context))
        }

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Evaluable>): Evaluable =
            Ne(newChildren[0], newChildren[1])

        override fun toString() = "(!= $left $right)"
    }

    open class Cmp(
        val name: String,
        val left: Evaluable,
        val right: Evaluable,
        val op: (Double, Double) -> Boolean,
    ) : Evaluable {
        override val returnType: Type
            get() = BoolType

        override fun eval(env: LocalRuntimeContext): Boolean =
            op(left.evalF64(env), right.evalF64(env))

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Evaluable>): Evaluable =
            Cmp(name, newChildren[0], newChildren[1], op)

        override fun toString() =
            "($name $left $right)"
    }

    class Ge(left: Evaluable, right: Evaluable) :
        Cmp(">=", left, right, { left, right -> left >= right })

    class Gt(left: Evaluable, right: Evaluable) :
        Cmp(">", left, right, { left, right -> left > right })

    class Le(left: Evaluable, right: Evaluable) :
        Cmp("<=", left, right, { left, right -> left <= right })

    class Lt(left: Evaluable, right: Evaluable) :
        Cmp("<", left, right, { left, right -> left < right })

    override fun toString() = "F64"
}