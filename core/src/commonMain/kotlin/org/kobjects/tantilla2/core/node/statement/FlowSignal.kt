package org.kobjects.tantilla2.core.node.statement

data class FlowSignal(
    val kind: Kind,
    val value: Any? = null) {

    enum class Kind {
        BREAK, CONTINUE, RETURN
    }

}