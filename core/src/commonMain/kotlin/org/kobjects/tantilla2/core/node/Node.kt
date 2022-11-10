package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.LocalRuntimeContext

class Node(
    private val name: String,
    vararg children: Evaluable,
    private val op: (List<Evaluable>, LocalRuntimeContext) -> Any?
) : Evaluable {
    val children = children.toList()

    override fun eval(env: LocalRuntimeContext): Any? {
        return op(children, env)
    }

    override fun children() = children

    override fun reconstruct(newChildren: List<Evaluable>) =
        Node(name, children = newChildren.toTypedArray(), op = op)

    override fun toString() =
        if (children.isEmpty()) "($name)" else "($name ${children.joinToString(" ")})"
}
