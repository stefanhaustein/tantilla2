package org.kobjects.tantilla2.core.node.statement

import org.kobjects.tantilla2.core.type.NoneType

data class FlowSignal(
    val kind: Kind,
    val value: Any = NoneType.None) {

    enum class Kind {
        BREAK, CONTINUE, RETURN
    }

}