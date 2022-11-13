package org.kobjects.tantilla2.core.node.statement

import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.VoidType
import org.kobjects.tantilla2.core.node.Evaluable

class BlockNode(
    vararg val statements: Evaluable
): Evaluable {

    override val returnType: Type
        get() = if (statements.isEmpty()) VoidType else statements.last().returnType

    override fun eval(env: LocalRuntimeContext): Any? {
        var result: Any? = null
        for (statement: Evaluable in statements) {
            result = statement.eval(env)
            if (result is FlowSignal) {
                return result
            }
        }
        return result
    }

    override fun children() = statements.asList()

    override fun reconstruct(newChildren: List<Evaluable>) =
        BlockNode(statements = newChildren.toTypedArray())

    override fun toString() =
        statements.joinToString(" ", prefix = "(begin ", postfix = ")")
}