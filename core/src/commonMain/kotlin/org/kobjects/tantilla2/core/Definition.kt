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
        LOCAL_VARIABLE, FUNCTION, CONST, CLASS, TRAIT, IMPL
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

    fun value(): Any?  {
        if (value == null && kind != Kind.CONST) {
            value = when (kind) {
                Kind.CONST -> value
                Kind.FUNCTION -> resolveFunction()
                Kind.CLASS -> resolveClass()
                Kind.TRAIT -> resolveTrait()
                Kind.IMPL -> resolveImpl()
                Kind.LOCAL_VARIABLE -> throw RuntimeException("Can't obtain local variable value from Definition.")
            }
        }
        return value
    }

    private fun resolveClass(): ParsingContext {
            println("Resolving class $name: $definitionText")
            val classContext = ParsingContext(name, ParsingContext.Kind.CLASS, parsingContext)
            val tokenizer = tokenizer()
            tokenizer.next()
            Parser.parse(tokenizer, classContext)
            println("Class successfully resolved!")
            return classContext
    }

    private fun resolveTrait(): ParsingContext {
            println("Resolving trait $name: $definitionText")
            val traitContext = ParsingContext(name, ParsingContext.Kind.TRAIT, parsingContext)
            val tokenizer = tokenizer()
            tokenizer.next()
            Parser.parse(tokenizer, traitContext)
            println("Trait successfully resolved!")
            return traitContext
    }

    private fun resolveImpl(): ParsingContext {
        println("Resolving impl $name: $definitionText")
        val implContext = ParsingContext(name, ParsingContext.Kind.IMPL, parsingContext)
        val tokenizer = tokenizer()
        tokenizer.next()
        Parser.parse(tokenizer, implContext)
        println("Impl successfully resolved!")
        return implContext
    }

    private fun resolveFunction(): Lambda {

            println("Resolving function $name: $definitionText")
            return Parser.parseLambda(tokenizer(), parsingContext)
    }

    override fun toString() = serialize()

    fun serialize(indent: String = "") =
        "$indent#start $name\n" +
        when (kind) {
            Kind.LOCAL_VARIABLE -> "${if (mutable) "var" else "let"} $name"
            Kind.FUNCTION -> "def $name ${if (value == null) definitionText else value!!.toString()}"
            Kind.TRAIT ->  "trait $name ${if (value == null) definitionText else value!!.toString()}"
            Kind.IMPL ->  "impl $name ${if (value == null) definitionText else value!!.toString()}"
            Kind.CLASS ->  "class $name ${if (value == null) definitionText else value!!.toString()}"
            Kind.CONST -> "const $name = $value"
        } +
                "\n$indent#end $name\n"
}