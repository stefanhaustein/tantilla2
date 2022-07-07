package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.RuntimeContext

class NativeEvaluable(val operation: (RuntimeContext) -> Any?): Evaluable<RuntimeContext> {
    override fun children() = emptyList<Evaluable<RuntimeContext>>()

    override fun eval(context: RuntimeContext): Any? = operation(context)

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) = this
}