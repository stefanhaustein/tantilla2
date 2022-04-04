package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.parser.Parser
import org.kobjects.tantilla2.parser.TantillaTokenizer
import org.kobjects.tantilla2.parser.TokenType
import typeOf

class Definition(
    val parsingContext: ParsingContext,
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

    fun type(): Type {
        if (type == null) {
            type = when (kind) {
                Kind.FUNCTION -> Parser.parseFunctionType(tokenizer(), parsingContext)
                Kind.LOCAL_VARIABLE -> throw RuntimeException("Local variable type must not be null")
                else -> typeOf(value())
            }
        }
        return type!!
    }

    fun value(): Any? =
        when (kind) {
            Kind.CONST -> value
            Kind.FUNCTION -> resolveFunction()
            Kind.CLASS -> resolveClass()
            Kind.LOCAL_VARIABLE -> throw RuntimeException("Can't obtain local variable value from Definition.")
        }

    private fun resolveClass(): ParsingContext {
        if (value == null) {
            println("Resolving class $name: $definitionText")
            val classContext = ParsingContext(name, ParsingContext.Kind.CLASS, parsingContext)
            val tokenizer = tokenizer()
            tokenizer.next()
            Parser.parse(tokenizer, classContext)
            println("Class successfully resolved!")
            value = classContext
        }
        return value as ParsingContext
    }

    private fun resolveFunction(): Lambda {
        if (value == null) {
            println("Resolving function $name: $definitionText")
            value = Parser.parseLambda(tokenizer(), parsingContext)
        }
        return value as Lambda
    }

    override fun toString() = serialize()

    fun serialize(indent: String = "") =
        "$indent#start $name\n" +
        when (kind) {
            Kind.LOCAL_VARIABLE -> "${if (mutable) "var" else "let"} $name"
            Kind.FUNCTION -> "def $name ${if (value == null) definitionText else value!!.toString()}"
            Kind.CLASS ->  "class $name ${if (value == null) definitionText else value!!.toString()}"
            Kind.CONST -> "const $name = $value"
        } +
                "\n$indent#end $name\n"
}