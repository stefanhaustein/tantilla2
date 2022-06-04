package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Control
import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.Type

class FlowControl(
    val kind: Control.FlowSignal.Kind,
    val expression: Evaluable<RuntimeContext>? = null) : TantillaNode {
    override val returnType: Type
        get() = TODO("Not yet implemented")

    override fun children(): List<Evaluable<RuntimeContext>> =
        if (expression == null) emptyList() else listOf(expression)

    override fun eval(context: RuntimeContext): Control.FlowSignal {
        val parameter = if (expression == null) null else expression.eval(context)
        return Control.FlowSignal(kind, parameter)
    }

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) =
        if (newChildren.size == 0) FlowControl(kind) else FlowControl(kind, newChildren[0])

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        if (expression == null) {
            writer.append("return")
        } else {
            writer.append("return ")
            writer.appendCode(expression)
        }
    }

}