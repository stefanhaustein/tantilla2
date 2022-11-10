package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.SerializableCode
import org.kobjects.tantilla2.core.Type

fun Evaluable.containsNode(node: Evaluable): Boolean {
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


interface TantillaNode : Evaluable, SerializableCode {


}