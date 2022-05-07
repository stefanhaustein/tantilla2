package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Void
import org.kobjects.tantilla2.core.RuntimeContext

class ErrorEvaluable(val errorMessage: String) : Evaluable<RuntimeContext> {
    override fun children(): List<Evaluable<RuntimeContext>> = emptyList()

    override fun eval(ctx: RuntimeContext) = throw RuntimeException(errorMessage)

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) = this

    override fun toString() = "Error: $errorMessage"

    override val type
        get() = Void

}