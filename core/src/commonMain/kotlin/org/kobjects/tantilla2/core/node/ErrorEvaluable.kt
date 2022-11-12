package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.builtin.VoidType

class ErrorEvaluable(val errorMessage: String) : Node() {
    override fun children(): List<Evaluable> = emptyList()

    override fun eval(ctx: LocalRuntimeContext) = throw RuntimeException(errorMessage)

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.append("Error: $errorMessage")
    }

    override val returnType
        get() = VoidType
}