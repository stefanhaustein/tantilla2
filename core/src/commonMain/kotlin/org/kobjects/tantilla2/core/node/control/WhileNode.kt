package org.kobjects.tantilla2.core.node.control

import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.node.Evaluable

class WhileNode(
    val condition: Evaluable,
    val body: Evaluable
): Evaluable {
    override fun eval(env: LocalRuntimeContext): FlowSignal? {
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
        return null
    }

    override fun children() = listOf(condition, body)

    override fun reconstruct(newChildren: List<Evaluable>) =
        WhileNode(newChildren[0], newChildren[1])

    override fun toString() = "(while $condition $body)"
}