package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.LocalRuntimeContext

abstract class AssignableNode : Node() {

    abstract fun assign(context: LocalRuntimeContext, value: Any?)
}