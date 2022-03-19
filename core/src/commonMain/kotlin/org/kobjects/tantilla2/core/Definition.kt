package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.parser.Parser
import org.kobjects.tantilla2.parser.TantillaTokenizer
import org.kobjects.tantilla2.parser.TokenType
import typeOf

class Definition(
    val name: String, // Not really necessary but should make debugging and printing easier.
    val kind: Kind,
    val definitionText: String = "",
    val index: Int = -1,
    val mutable: Boolean = false,
    private var type: Type? = null,
    private var value: Any? = null,
) {

    enum class Kind {
        LOCAL_VARIABLE, FUNCTION, CONST, CLASS
    }

    private fun tokenizer(): TantillaTokenizer {
        val tokenizer = TantillaTokenizer(definitionText)
        tokenizer.consume(TokenType.BOF)
        return tokenizer
    }

    fun type(parsingContext: ParsingContext): Type {
        if (type == null) {
            type = when (kind) {
                Kind.CLASS -> MetaType(value(parsingContext) as ParsingContext)
                Kind.FUNCTION -> Parser.parseFunctionType(tokenizer(), parsingContext)
                Kind.LOCAL_VARIABLE -> throw RuntimeException("Local variable type must not be null")
                Kind.CONST -> typeOf(value)
            }
        }
        return type!!
    }

    fun value(parsingContext: ParsingContext): Any? =
        when (kind) {
            Kind.CONST -> value
            Kind.FUNCTION -> resolveFunction(parsingContext)
            Kind.CLASS -> resolveClass(parsingContext)
            Kind.LOCAL_VARIABLE -> throw RuntimeException("Can't obtain local variable value from Definition.")
        }

    private fun resolveClass(parsingContext: ParsingContext): ParsingContext {
        if (value == null) {
            val classContext = ParsingContext(name, ParsingContext.Kind.CLASS, parsingContext)
            Parser.parseRoot(tokenizer(), classContext)
            value = classContext
        }
        return value as ParsingContext
    }

    private fun resolveFunction(parsingContext: ParsingContext): Lambda {
        if (value == null) {
            value = Parser.parseLambda(tokenizer(), parsingContext)
        }
        return value as Lambda
    }

    override fun toString(): String =
        when (kind) {
            Kind.LOCAL_VARIABLE -> "${if (mutable) "var" else "let"} $name"
            Kind.FUNCTION -> "def $name ${if (value == null) definitionText else value!!.toString()}"
            Kind.CLASS ->  "class $name ${if (value == null) definitionText else value!!.toString()}"
            Kind.CONST -> "const $name = $value"
        }
}