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
    private var currentValue = explicitValue

    enum class Kind {
        LOCAL_VARIABLE, STATIC_VARIABLE, FUNCTION, CLASS, TRAIT, IMPL, UNPARSEABLE
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
        if (currentValue == null) {
            currentValue = when (kind) {
                Kind.STATIC_VARIABLE -> null
                Kind.FUNCTION -> resolveFunction()
                Kind.CLASS -> resolveClass()
                Kind.TRAIT -> resolveTrait()
                Kind.IMPL -> resolveImpl()
                Kind.UNPARSEABLE -> throw RuntimeException("Can't obtain value for unparseable definition.")
                Kind.LOCAL_VARIABLE -> throw RuntimeException("Can't obtain local variable value from Definition.")
            }
        }
        return currentValue
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


    fun serializeTitle(writer: CodeWriter) {
        when (kind) {
            Kind.STATIC_VARIABLE,
            Kind.LOCAL_VARIABLE -> writer.keyword(if (mutable) "var " else "let ")
                .declaration(name)
                .append(if (explicitType != null) ": " + explicitType.typeName else "")
            Kind.FUNCTION -> writer.keyword("def ").declaration(name).appendType(type())
            Kind.TRAIT,
            Kind.IMPL,
            Kind.CLASS -> writer.keyword(kind.name.lowercase()).append(' ').declaration(name)
            Kind.UNPARSEABLE -> writer.append("(unparseable: $name)")
        }
    }


    override fun serializeCode(writer: CodeWriter, precedence: Int) {
       when (kind) {
           Kind.STATIC_VARIABLE,
           Kind.LOCAL_VARIABLE -> {
               serializeTitle(writer)
               if (initializer != null) {
                   writer.append(" = ")
                   writer.appendCode(initializer)
               }
           }
           Kind.UNPARSEABLE -> writer.append(definitionText)
           Kind.TRAIT,
           Kind.CLASS,
           Kind.IMPL -> {
               if (currentValue != null) {
                   writer.appendCode(currentValue)
               } else {
                   writer.append(definitionText)
               }
           }
           Kind.FUNCTION -> {
               writer.keyword("def ").declaration(name)
               if (currentValue != null) {
                   writer.appendCode(currentValue)
               } else {
                    writer.append(definitionText)
               }
           }
       }
    }



    fun index() = scope.locals.indexOf(name)
}