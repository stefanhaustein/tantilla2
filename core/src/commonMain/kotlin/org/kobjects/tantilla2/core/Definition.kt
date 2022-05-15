package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.classifier.ClassDefinition
import org.kobjects.tantilla2.core.classifier.ImplDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.function.Lambda
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.parser.TantillaTokenizer
import org.kobjects.tantilla2.core.parser.TokenType

class Definition(
    val scope: Scope,
    val name: String, // Not really necessary but should make debugging and printing easier.
    val kind: Kind,
    val builtin: Boolean = false,
    val definitionText: String = "",
    val mutable: Boolean = false,
    private val explicitType: Type? = null,
    private val explicitValue: Any? = null,
    var initializer: Evaluable<RuntimeContext>? = null,
    var docString: String = "",
) : SerializableCode {

    private var cachedType = explicitType
    private var cachedValue = explicitValue

    enum class Kind {
        LOCAL_VARIABLE, FUNCTION, CLASS, TRAIT, IMPL, UNPARSEABLE
    }

    private fun tokenizer(): TantillaTokenizer {
        val tokenizer = TantillaTokenizer(definitionText)
        tokenizer.consume(TokenType.BOF)
        return tokenizer
    }

    fun type(): Type {
        if (cachedType == null) {
            cachedType = when (kind) {
                Kind.FUNCTION -> Parser.parseFunctionType(tokenizer(), scope)
                Kind.LOCAL_VARIABLE -> throw RuntimeException("Local variable type must not be null")
                else -> value().type
            }
        }
        return cachedType!!
    }

    fun value(): Any?  {
        if (cachedValue == null) {
            cachedValue = when (kind) {
                Kind.FUNCTION -> resolveFunction()
                Kind.CLASS -> resolveClass()
                Kind.TRAIT -> resolveTrait()
                Kind.IMPL -> resolveImpl()
                Kind.UNPARSEABLE -> throw RuntimeException("Can't obtain value for unparseable definition.")
                Kind.LOCAL_VARIABLE -> throw RuntimeException("Can't obtain local variable value from Definition.")
            }
        }
        return cachedValue
    }

    private fun resolveClass(): Scope {
            println("Resolving class $name: $definitionText")
            val classContext = ClassDefinition(name, scope)
            val tokenizer = tokenizer()
            tokenizer.next()
            Parser.parse(tokenizer, classContext)
            println("Class successfully resolved!")
            return classContext
    }

    private fun resolveTrait(): Scope {
            println("Resolving trait $name: $definitionText")
            val traitContext = TraitDefinition(name, scope)
            val tokenizer = tokenizer()
            tokenizer.next()
            Parser.parse(tokenizer, traitContext)
            println("Trait successfully resolved!")
            return traitContext
    }

    private fun resolveImpl(): Scope {
        println("Resolving impl $name: $definitionText")
        val traitName = name.substring(0, name.indexOf(' '))
        val trait = scope.resolve(traitName).value() as TraitDefinition
        val className = name.substring(name.lastIndexOf(' ') + 1)
        val implFor = scope.resolve(className).value() as ClassDefinition
        val implContext = ImplDefinition(name, scope, trait, implFor)
        val tokenizer = tokenizer()
        tokenizer.next()
        Parser.parse(tokenizer, implContext)
        println("Impl successfully resolved!")
        return implContext
    }

    private fun resolveFunction(): Lambda {

            println("Resolving function $name: $definitionText")
            return Parser.parseLambda(tokenizer(), scope)
    }


    override fun toString() = serializeCode()

    fun title(writer: CodeWriter) {
        serialize(writer, true)
    }

    fun serialize(writer: CodeWriter, titleOnly: Boolean) {
        when (kind) {
            Kind.LOCAL_VARIABLE -> writer.keyword(if (mutable) "var " else "let ")
                .declaration(name)
                .append(if (explicitType != null) ": " + explicitType.typeName else "")
            Kind.FUNCTION -> writer.keyword("def ").declaration(name).append(type().typeName)
            Kind.TRAIT -> writer.keyword("trait ").declaration(name)
            Kind.IMPL -> writer.keyword("impl ").declaration(name)
            Kind.CLASS -> writer.keyword("class ").declaration(name)
            Kind.UNPARSEABLE -> if (titleOnly) {
                writer.append("(unparseable: $name)")
            }
        }
        if (titleOnly) {
            return
        }
        if (kind == Kind.LOCAL_VARIABLE) {
            if (initializer != null) {
                writer.append(" = ")
                serializeCode(writer)
            }
        } else if (kind == Kind.UNPARSEABLE) {
            writer.append(definitionText)
        } else if (cachedValue == null) {
            writer.append(definitionText)
        } else {
            writer.append(":")
            writer.indent()
            writer.newline()
            cachedValue.serializeCode(writer)
        }
    }


    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        serialize(writer, false)
    }


    fun index() = scope.locals.indexOf(name)
}