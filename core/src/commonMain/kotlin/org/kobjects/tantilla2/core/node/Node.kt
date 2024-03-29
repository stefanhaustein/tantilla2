package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Evaluable
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.SerializableCode
import org.kobjects.tantilla2.core.type.Type

/**
 * Abstract serializable evaluable with toString implemented based on serialization.
 */

abstract class Node : Evaluable, SerializableCode {

    abstract val returnType: Type

    final override fun toString() = CodeWriter().appendCode(this).toString()

    abstract fun children(): List<Node>

    abstract fun reconstruct(newChildren: List<Node>): Evaluable

    fun containsNode(node: Node): Boolean {
        if (this == node) {
            return true
        }
        for (child in children()) {
            if (child.containsNode(node)) {
                return true
            }
        }
        return false
    }

    open fun requireAssignability(): Node = throw UnsupportedOperationException("Target '$this' is not assignable.")

    open fun assign(context: LocalRuntimeContext, value: Any): Unit =
        throw UnsupportedOperationException()

    fun recurse(task: (Node) -> Unit) {
        for (child in children()) {
            child.recurse(task)
        }
        task(this)
    }
}