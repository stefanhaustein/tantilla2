package org.kobjects.tantilla2.core.node.control

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.builtin.VoidType
import org.kobjects.tantilla2.core.node.Evaluable
import org.kobjects.tantilla2.core.node.Node

class ForNode(
    val iteratorName: String,
    val iteratorIndex: Int,
    val rangeExpression: Evaluable,
    val bodyExpression: Evaluable,
) : Node() {
    override val returnType: Type
        get() = VoidType

    override fun children() = listOf(rangeExpression, bodyExpression)

    override fun eval(ctx: LocalRuntimeContext): Any? {
        val iterable = rangeExpression.eval(ctx) as Iterable<*>
        for (i in iterable) {
            ctx.checkState(this)
            ctx.variables[iteratorIndex] = i
            val value = bodyExpression.eval(ctx)
            if (value is FlowSignal) {
                when (value.kind) {
                    FlowSignal.Kind.BREAK -> break
                    FlowSignal.Kind.CONTINUE -> continue
                    FlowSignal.Kind.RETURN -> return value.value
                }
            }
        }
        return null
    }

    override fun reconstruct(newChildren: List<Evaluable>) =
        ForNode(iteratorName, iteratorIndex, newChildren[0], newChildren[1])

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.append("for ").append(iteratorName).append(" in ")
        writer.appendCode(rangeExpression)
        writer.append(':').indent().newline()
        writer.appendCode(bodyExpression)
        writer.outdent()
    }


}