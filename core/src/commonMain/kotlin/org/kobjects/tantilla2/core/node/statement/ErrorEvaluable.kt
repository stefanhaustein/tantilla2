package org.kobjects.tantilla2.core.node.statement

import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.type.NoneType
import org.kobjects.tantilla2.core.node.LeafNode

class ErrorEvaluable(val body: String, val exception: ParsingException) : LeafNode() {

    override fun eval(ctx: LocalRuntimeContext) = throw RuntimeException(exception)

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.append(body)
    }

    override val returnType
        get() = NoneType
}