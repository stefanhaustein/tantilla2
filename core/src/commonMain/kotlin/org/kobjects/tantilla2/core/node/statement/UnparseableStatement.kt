package org.kobjects.tantilla2.core.node.statement

import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.definition.CodeFragment
import org.kobjects.tantilla2.core.type.NoneType
import org.kobjects.tantilla2.core.node.LeafNode

class UnparseableStatement(val body: CodeFragment, val exception: ParsingException) : LeafNode() {

    override fun eval(ctx: LocalRuntimeContext) = throw RuntimeException(exception)

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.appendWrapped(CodeWriter.Kind.ERROR, body.code)
    }

    override val returnType
        get() = NoneType
}