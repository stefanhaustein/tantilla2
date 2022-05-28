package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.runtime.Void

class ErrorEvaluable(val errorMessage: String) : TantillaNode {
    override fun children(): List<Evaluable<RuntimeContext>> = emptyList()

    override fun eval(ctx: RuntimeContext) = throw RuntimeException(errorMessage)

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) = this

    override fun toString() = "Error: $errorMessage"

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.append(toString())
    }

    override val returnType
        get() = Void
}