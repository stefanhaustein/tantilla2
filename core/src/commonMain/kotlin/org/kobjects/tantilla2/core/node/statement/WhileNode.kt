package org.kobjects.tantilla2.core.node.statement

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.NoneType
import org.kobjects.tantilla2.core.node.Node

class WhileNode(
    val condition: Node,
    val body: Node
): Node() {
    override val returnType: Type
        get() = NoneType

    override fun eval(env: LocalRuntimeContext): Any {
        while (condition.eval(env) as Boolean) {
            val result = body.eval(env)
            if (result is FlowSignal) {
                when (result.kind) {
                    FlowSignal.Kind.BREAK -> break
                    FlowSignal.Kind.CONTINUE -> continue
                    FlowSignal.Kind.RETURN -> return result
                }
            }
        }
        return NoneType.None
    }

    override fun children() = listOf(condition, body)

    override fun reconstruct(newChildren: List<Node>) =
        WhileNode(newChildren[0], newChildren[1])


    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.appendKeyword("while ")
        writer.appendCode(condition)
        writer.append(':').indent().newline()
        writer.appendCode(body)
        writer.outdent()
    }
}