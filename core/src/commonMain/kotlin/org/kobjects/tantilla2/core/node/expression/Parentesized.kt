package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Evaluable
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.node.AssignableNode
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.Type

class Parentesized(val expression: Node) : AssignableNode() {
    override fun assign(context: LocalRuntimeContext, value: Any) {
        (expression as AssignableNode).assign(context, value)
    }

    override fun children() = listOf(expression)

    override fun reconstruct(newChildren: List<Node>) = Parentesized(newChildren.first())

    override fun eval(context: LocalRuntimeContext) = expression.eval(context)

    override val returnType: Type
        get() = expression.returnType

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.appendInParens(expression)
    }
}