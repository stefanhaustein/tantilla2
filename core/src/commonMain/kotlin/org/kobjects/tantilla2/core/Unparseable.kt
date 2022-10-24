package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Bool
import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.parser.TantillaTokenizer
import org.kobjects.tantilla2.core.parser.TokenType

class Unparseable(
    override val parentScope: Scope?,
    override val name: String = "(unparseable)",
    val definitionText: String
) : Definition {

    override val kind: Definition.Kind
        get() = Definition.Kind.UNPARSEABLE

    override fun getValue(self: Any?) = throw UnsupportedOperationException()

    override val type: Type
        get() = throw UnsupportedOperationException()

    override fun resolveAll(compilationResults: CompilationResults) = false

    override fun serializeSummary(writer: CodeWriter) {
        serializeCode(writer)
    }

    override fun serializeTitle(writer: CodeWriter, abbreviated: Boolean) {
        writer.append("(error: $name)")
    }

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.append(definitionText)
    }

    companion object {
        fun fromDisabledCode(parentScope: Scope?, code: String): Unparseable {
            val content = code.substring(4, code.length - 4)
            val tokenizer = TantillaTokenizer(content)
            tokenizer.consume(TokenType.BOF)
            var name = "Error #${parentScope?.count() ?: 1}"
            if (tokenizer.current.type == TokenType.IDENTIFIER) {
                // TODO: Parse definition and check for leftovers...
                tokenizer.next()
                if (tokenizer.current.type == TokenType.IDENTIFIER) {
                    name = tokenizer.current.text
                }
            }
            return Unparseable(parentScope, name, content)
        }
    }
}