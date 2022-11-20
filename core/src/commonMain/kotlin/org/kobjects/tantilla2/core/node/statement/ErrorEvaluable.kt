package org.kobjects.tantilla2.core.node.statement

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.type.VoidType
import org.kobjects.tantilla2.core.node.LeafNode

class ErrorEvaluable(val errorMessage: String) : LeafNode() {

    override fun eval(ctx: LocalRuntimeContext) = throw RuntimeException(errorMessage)

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.append("Error: $errorMessage")
    }

    override val returnType
        get() = VoidType
}