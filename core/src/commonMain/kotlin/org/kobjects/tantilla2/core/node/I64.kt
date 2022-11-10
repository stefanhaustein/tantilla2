package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.LocalRuntimeContext

/**
 * Operations.
 */
object I64 {

    class Const(
        val value: Long
    ): Evaluable {
        override fun eval(ctx: LocalRuntimeContext) = value

        override fun evalI64(ctx: LocalRuntimeContext) = value

        override fun children() = listOf<Evaluable>()

        override fun reconstruct(newChildren: List<Evaluable>) = this

        override fun toString() = value.toString()
    }

    open class Binary(
        private val name: String,
        private val left: Evaluable,
        private val right: Evaluable,
        private val op: (Long, Long) -> Long
    ) : Evaluable {

        override fun eval(context: LocalRuntimeContext): Long =
            op(left.evalI64(context), right.evalI64(context))

        override fun evalI64(context: LocalRuntimeContext): Long =
            op(left.evalI64(context), right.evalI64(context))

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

    open class Unary(
        private val name: String,
        private val arg: Evaluable,
        private val op: (Long) -> Long
    ) : Evaluable {
        override fun eval(ctx: LocalRuntimeContext): Long =
            op(arg.evalI64(ctx))

        override fun evalI64(ctx: LocalRuntimeContext): Long =
            op(arg.evalI64(ctx))

        override fun children() = listOf(arg)

        override fun reconstruct(newChildren: List<Evaluable>): Evaluable = Unary(name, newChildren[0], op)

        override fun toString(): String = "($name $arg)"
    }
    class Neg(arg: Evaluable) : Unary("neg", arg, { -it })

    class Eq(
        val left: Evaluable,
        val right: Evaluable,
    ): Evaluable {
        override fun eval(context: LocalRuntimeContext): Boolean {
            return (left.evalI64(context) == right.evalI64(context))
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
        override fun eval(context: LocalRuntimeContext): Boolean {
            return (left.evalI64(context) == right.evalI64(context))
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
        val op: (Long, Long) -> Boolean,
    ) : Evaluable {
        override fun eval(env: LocalRuntimeContext): Boolean =
            op(left.evalI64(env), right.evalI64(env))

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

    override fun toString() = "I64"
}