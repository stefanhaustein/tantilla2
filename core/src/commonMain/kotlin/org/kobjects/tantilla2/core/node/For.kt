package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Control
import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.runtime.Range
import org.kobjects.tantilla2.core.runtime.Void

class For(
    val iteratorName: String,
    val iteratorIndex: Int,
    val rangeExpression: Evaluable<RuntimeContext>,
    val bodyExpression: Evaluable<RuntimeContext>,
) : TantillaNode {
    override val returnType: Type
        get() = Void

    override fun children() = listOf(rangeExpression, bodyExpression)

    override fun eval(ctx: RuntimeContext): Any? {
        val iterable = rangeExpression.eval(ctx) as Iterable<*>
        for (i in iterable) {
            ctx.variables[iteratorIndex] = i
            val value = bodyExpression.eval(ctx)
            if (value is Control.FlowSignal) {
                when (value.kind) {
                    Control.FlowSignal.Kind.BREAK -> break
                    Control.FlowSignal.Kind.CONTINUE -> continue
                    Control.FlowSignal.Kind.RETURN -> return value.value
                }
            }
        }
        return null
    }

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) =
        For(iteratorName, iteratorIndex, newChildren[0], newChildren[1])

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.append("for ").append(iteratorName).append(" in ")
        writer.appendCode(rangeExpression)
        writer.append(':').indent().newline()
        writer.appendCode(bodyExpression)
        writer.outdent()
    }


}