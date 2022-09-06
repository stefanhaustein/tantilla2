package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.builtin.Void

class ErrorEvaluable(val errorMessage: String) : TantillaNode {
    override fun children(): List<Evaluable<LocalRuntimeContext>> = emptyList()

    override fun eval(ctx: LocalRuntimeContext) = throw RuntimeException(errorMessage)

    override fun reconstruct(newChildren: List<Evaluable<LocalRuntimeContext>>) = this

    override fun toString() = "Error: $errorMessage"

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.append(toString())
    }

    override val returnType
        get() = Void
}