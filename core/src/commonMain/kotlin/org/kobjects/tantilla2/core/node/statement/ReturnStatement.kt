package org.kobjects.tantilla2.core.node.statement

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.control.LoopControlSignal
import org.kobjects.tantilla2.core.control.ReturnSignal
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.NoneType

class ReturnStatement(
    val expression: Node? = null) : Node() {
    override val returnType: Type
        get() = NoneType

    override fun children(): List<Node> =
        if (expression == null) emptyList() else listOf(expression)

    override fun eval(context: LocalRuntimeContext) {
        throw ReturnSignal(expression?.eval(context) ?: NoneType.None)
    }

    override fun reconstruct(newChildren: List<Node>) =
        if (newChildren.isEmpty()) ReturnStatement(null) else ReturnStatement(newChildren[0])

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.appendKeyword("return")
        if (expression != null) {
            writer.append(' ')
            writer.appendCode(expression)
        }
    }
}