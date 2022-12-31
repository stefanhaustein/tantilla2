package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Evaluable
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.SerializableCode

/**
 * Abstract serializable evaluable with toString implemented based on serialization.
 */

abstract class Node : Evaluable, SerializableCode {

    final override fun toString() = CodeWriter().appendCode(this).toString()

    abstract fun children(): List<Node>

    abstract fun reconstruct(newChildren: List<Node>): Evaluable

    fun containsNode(node: Evaluable): Boolean {
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

    open fun isAssignable() = false

    open fun assign(context: LocalRuntimeContext, value: Any): Unit =
        throw UnsupportedOperationException()
}