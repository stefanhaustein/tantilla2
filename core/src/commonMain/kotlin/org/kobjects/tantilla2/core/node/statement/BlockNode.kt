package org.kobjects.tantilla2.core.node.statement

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.VoidType
import org.kobjects.tantilla2.core.node.Node

class BlockNode(
    vararg val statements: Node
): Node() {

    override val returnType: Type
        get() = if (statements.isEmpty()) VoidType else statements.last().returnType


    override fun eval(env: LocalRuntimeContext): Any? {
        var result: Any? = null
        for (statement: Node in statements) {
            result = statement.eval(env)
            if (result is FlowSignal) {
                return result
            }
        }
        return result
    }

    override fun children() = statements.asList()

    override fun reconstruct(newChildren: List<Node>) =
        BlockNode(statements = newChildren.toTypedArray())

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        if (statements.size > 0) {
            writer.appendCode(statements[0])
            for (i in 1 until statements.size) {
               writer.newline()
                writer.appendCode(statements[i])
            }
        }
    }
}