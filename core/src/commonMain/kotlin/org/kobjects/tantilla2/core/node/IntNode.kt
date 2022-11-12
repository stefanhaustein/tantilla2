package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.Precedence
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.builtin.BoolType
import org.kobjects.tantilla2.core.builtin.IntType

/**
 * Operations.
 */
object IntNode {

    class Const(
        val value: Long
    ): Node() {
        override val returnType: Type
            get() = IntType

        override fun eval(ctx: LocalRuntimeContext) = value

        override fun evalI64(ctx: LocalRuntimeContext) = value

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
        private val op: (Long, Long) -> Long
    ) : Node() {
        override val returnType: Type
            get() = IntType

        override fun eval(context: LocalRuntimeContext): Long =
            op(left.evalI64(context), right.evalI64(context))

        override fun evalI64(context: LocalRuntimeContext): Long =
            op(left.evalI64(context), right.evalI64(context))

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

    open class Unary(
        private val name: String,
        private val precedence: Int,
        private val arg: Evaluable,
        private val op: (Long) -> Long
    ) : Node() {
        override val returnType: Type
            get() = IntType

        override fun eval(ctx: LocalRuntimeContext): Long =
            op(arg.evalI64(ctx))

        override fun evalI64(ctx: LocalRuntimeContext): Long =
            op(arg.evalI64(ctx))

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
    class Neg(arg: Evaluable) : Unary("-", Precedence.NEG, arg, { -it })

    class Eq(
        val left: Evaluable,
        val right: Evaluable,
    ): Node() {
        override val returnType: Type
            get() = BoolType

        override fun eval(context: LocalRuntimeContext): Boolean {
            return (left.evalI64(context) == right.evalI64(context))
        }

        override fun children() = listOf(left, right)
        override fun reconstruct(newChildren: List<Evaluable>): Evaluable =
            Eq(newChildren[0], newChildren[1])

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.appendInfix(this, parentPrecedence, "==", Precedence.EQUALITY)
        }
    }

    class Ne(
        val left: Evaluable,
        val right: Evaluable,
    ): Node() {
        override val returnType: Type
            get() = BoolType

        override fun eval(context: LocalRuntimeContext): Boolean {
            return (left.evalI64(context) == right.evalI64(context))
        }

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Evaluable>): Evaluable =
            Ne(newChildren[0], newChildren[1])

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.appendInfix(this, parentPrecedence, "!=", Precedence.EQUALITY)
        }
    }

    open class Cmp(
        val name: String,
        val left: Evaluable,
        val right: Evaluable,
        val op: (Long, Long) -> Boolean,
    ) : Node() {
        override val returnType: Type
            get() = BoolType

        override fun eval(env: LocalRuntimeContext): Boolean =
            op(left.evalI64(env), right.evalI64(env))

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Evaluable>): Evaluable =
            Cmp(name, newChildren[0], newChildren[1], op)

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.appendInfix(this, parentPrecedence, name, Precedence.RELATIONAL)
        }
    }

    class Ge(left: Evaluable, right: Evaluable) :
        Cmp(">=", left, right, { left, right -> left >= right })

    class Gt(left: Evaluable, right: Evaluable) :
        Cmp(">", left, right, { left, right -> left > right })

    class Le(left: Evaluable, right: Evaluable) :
        Cmp("<=", left, right, { left, right -> left <= right })

    class Lt(left: Evaluable, right: Evaluable) :
        Cmp("<", left, right, { left, right -> left < right })

}