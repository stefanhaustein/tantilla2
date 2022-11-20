package org.kobjects.tantilla2.core.node.statement

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.node.Node

class FlowControlNode(
    val kind: FlowSignal.Kind,
    val expression: Node? = null) : Node() {
    override val returnType: Type
        get() = TODO("Not yet implemented")

    override fun children(): List<Node> =
        if (expression == null) emptyList() else listOf(expression)

    override fun eval(context: LocalRuntimeContext): FlowSignal {
        val parameter = if (expression == null) null else expression.eval(context)
        return FlowSignal(kind, parameter)
    }

    override fun reconstruct(newChildren: List<Node>) =
        if (newChildren.size == 0) FlowControlNode(kind) else FlowControlNode(kind, newChildren[0])

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        if (expression == null) {
            writer.append("return")
        } else {
            writer.append("return ")
            writer.appendCode(expression)
        }
    }

}