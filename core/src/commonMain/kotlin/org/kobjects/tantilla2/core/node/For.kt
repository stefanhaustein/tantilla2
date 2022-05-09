package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Type
import org.kobjects.greenspun.core.Void
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.runtime.Range
import org.kobjects.tantilla2.core.serialize

class For(
    val iteratorName: String,
    val iteratorIndex: Int,
    val rangeExpression: Evaluable<RuntimeContext>,
    val bodyExpression: Evaluable<RuntimeContext>,
) : Evaluable<RuntimeContext>, Serializable {
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

    override fun serialize(writer: CodeWriter, prcedence: Int) {
        writer.append("for ").append(iteratorName).append(" in ")
        rangeExpression.serialize(writer)
        writer.append(':').indent().newline()
        bodyExpression.serialize(writer)
        writer.outdent()
    }


}