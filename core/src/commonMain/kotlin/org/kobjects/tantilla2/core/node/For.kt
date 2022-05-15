package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Type
import org.kobjects.greenspun.core.Void
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.SerializableCode
import org.kobjects.tantilla2.core.runtime.Range
import org.kobjects.tantilla2.core.serializeCode

class For(
    val iteratorName: String,
    val iteratorIndex: Int,
    val rangeExpression: Evaluable<RuntimeContext>,
    val bodyExpression: Evaluable<RuntimeContext>,
) : Evaluable<RuntimeContext>, SerializableCode {
    override val type: Type
        get() = Void

    override fun children() = listOf(rangeExpression, bodyExpression)

    override fun eval(ctx: RuntimeContext): Any? {
        val range = rangeExpression.eval(ctx) as Range
        for (i in range.start.toInt() until range.end.toInt()) {
            ctx.variables[iteratorIndex] = i.toDouble()
            bodyExpression.eval(ctx)
        }
        return null
    }

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) =
        For(iteratorName, iteratorIndex, newChildren[0], newChildren[1])

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.append("for ").append(iteratorName).append(" in ")
        rangeExpression.serializeCode(writer)
        writer.append(':').indent().newline()
        bodyExpression.serializeCode(writer)
        writer.outdent()
    }


}