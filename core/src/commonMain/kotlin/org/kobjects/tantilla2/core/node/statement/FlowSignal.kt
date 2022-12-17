package org.kobjects.tantilla2.core.node.statement

import org.kobjects.tantilla2.core.type.VoidType

data class FlowSignal(
    val kind: Kind,
    val value: Any = VoidType.None) {

    enum class Kind {
        BREAK, CONTINUE, RETURN
    }

}