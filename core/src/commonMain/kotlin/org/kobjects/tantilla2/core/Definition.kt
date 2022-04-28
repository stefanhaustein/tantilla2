package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.classifier.ClassDefinition
import org.kobjects.tantilla2.core.classifier.ImplDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.parser.TantillaTokenizer
import org.kobjects.tantilla2.core.parser.TokenType
import tantillaName
import type

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
) {

    private var cachedType = explicitType
    private var cachedValue = explicitValue

    enum class Kind {
        LOCAL_VARIABLE, FUNCTION, CONST, CLASS, TRAIT, IMPL, UNPARSEABLE
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
        if (cachedValue == null && kind != Kind.CONST) {
            cachedValue = when (kind) {
                Kind.CONST -> explicitValue
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

    // TODO: Move up to scope so it can be used with null to add a new value.
    fun replace(newContent: String) {
        val tokenizer = TantillaTokenizer(newContent)
        tokenizer.next()
        var replacement: Definition
        try {
            replacement = Parser.parseDefinition(tokenizer, scope, 0)
        } catch (e: Exception) {
            replacement = Definition(scope, name, Kind.UNPARSEABLE, definitionText = newContent)
        }
        scope.definitions.remove(name)
        scope.definitions[replacement.name] = replacement
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

    private fun resolveFunction(): Callable {

            println("Resolving function $name: $definitionText")
            return Parser.parseLambda(tokenizer(), scope)
    }

    override fun toString() = serialize()

    fun title() = when (kind) {
        Kind.LOCAL_VARIABLE -> "${if (mutable) "var" else "let"} $name${if (explicitType != null) ": " + explicitType.tantillaName else ""}"
        Kind.FUNCTION -> "def $name ${type()}"
        Kind.TRAIT ->  "trait $name"
        Kind.IMPL ->  "impl $name"
        Kind.CLASS ->  "class $name"
        Kind.UNPARSEABLE -> "(unparseable: $name)"
        Kind.CONST -> "const $name = $explicitValue"
    }

    fun serialize(indent: String = "") =
      //  "$indent#start $name\n" +
        when (kind) {
            Kind.LOCAL_VARIABLE -> "${if (mutable) "var" else "let"} $name"
            Kind.CONST -> "const $name = $explicitValue"
            Kind.FUNCTION -> "def $name ${if (cachedValue == null) definitionText else cachedValue.serialize()}"
            Kind.TRAIT ->  "trait $name ${if (cachedValue == null) definitionText else cachedValue.serialize()}"
            Kind.IMPL ->  "impl $name ${if (cachedValue == null) definitionText else cachedValue.serialize()}"
            Kind.CLASS ->  "class $name ${if (cachedValue == null) definitionText else cachedValue.serialize()}"
            Kind.UNPARSEABLE -> definitionText
        }
        //        "\n$indent#end $name\n"

    fun index() = scope.locals.indexOf(name)
}