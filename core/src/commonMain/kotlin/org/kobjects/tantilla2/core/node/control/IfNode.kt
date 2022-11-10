package org.kobjects.tantilla2.core.node.control

import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.node.Evaluable

class IfNode(
    vararg val ifThenElse: Evaluable,
) : Evaluable {
    override fun eval(env: LocalRuntimeContext): Any? {
        for (i in ifThenElse.indices step 2) {
            if (i == ifThenElse.size - 1) {
                return ifThenElse[i].eval(env)
            } else if (ifThenElse[i].eval(env) as Boolean) {
                return ifThenElse[i + 1].eval(env)
            }
        }
        return Unit
    }

    override fun children() = ifThenElse.toList()

    override fun reconstruct(newChildren: List<Evaluable>) = IfNode(*newChildren.toTypedArray())

    override fun toString() ="(if ${ifThenElse.joinToString(" ")})"
}