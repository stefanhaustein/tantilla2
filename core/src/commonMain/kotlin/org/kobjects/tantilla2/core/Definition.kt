package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.classifier.UserClassDefinition
import org.kobjects.tantilla2.core.classifier.ImplDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Lambda
import org.kobjects.tantilla2.core.node.TantillaNode
import org.kobjects.tantilla2.core.parser.Parser
import org.kobjects.tantilla2.core.parser.TantillaTokenizer
import org.kobjects.tantilla2.core.parser.TokenType

class Definition(
    val scope: Scope,
    val name: String, // Not really necessary but should make debugging and printing easier.
    val kind: Kind,
    val definitionText: String = "",
    val mutable: Boolean = false,
    private var resolvedType: Type? = null,
    private var resolvedValue: Any? = UnresolvedValue,
    var docString: String = "",
) : SerializableCode {
    var error: Exception? = null

    private var resolvedInitializer: Evaluable<RuntimeContext>? = UnresolvedEvalueable

    enum class Kind {
        LOCAL_VARIABLE, STATIC_VARIABLE, FUNCTION, CLASS, TRAIT, IMPL, UNPARSEABLE
    }

    private fun tokenizer(): TantillaTokenizer {
        val tokenizer = TantillaTokenizer(definitionText)
        tokenizer.consume(TokenType.BOF)
        error = null
        return tokenizer
    }

    private fun exceptionInResolve(e: Exception, tokenizer: TantillaTokenizer): Exception {
        if (e is ParsingException) {
            error = e
        } else {
            error = ParsingException(tokenizer.current, e.message ?: "Parsing Error", e)
        }
        error!!.printStackTrace()
        throw error!!
    }

    fun error(): Exception? {
        if (error == null) {
            try {
                when (kind) {
                    Kind.LOCAL_VARIABLE,
                    Kind.STATIC_VARIABLE -> initializer()
                    Kind.UNPARSEABLE -> null
                    else -> value()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return error;
    }

    fun hasError(recursive: Boolean): Boolean {
        if (error() != null) {
            return true
        }
        if (recursive && isScope()) {
            return (value() as Scope).hasError()
        }
        return false
    }


    fun type(): Type {
        if (resolvedType == null) {
            val tokenizer = tokenizer()
            try {
                when (kind) {
                    Kind.FUNCTION -> resolvedType = Parser.parseFunctionType(tokenizer, scope)
                    Kind.LOCAL_VARIABLE,
                    Kind.STATIC_VARIABLE -> resolveVariable(tokenizer, typeOnly = true)
                    else -> resolvedType = value().type
                }
            } catch (e: Exception) {
                throw exceptionInResolve(e, tokenizer)
            }
        }
        return resolvedType!!
    }

    fun value(): Any?  {
        if (resolvedValue == UnresolvedValue) {
            val tokenizer = tokenizer()
            try {
                when (kind) {
                    Kind.STATIC_VARIABLE -> resolveVariable(tokenizer)
                    Kind.FUNCTION -> resolvedValue =  Parser.parseLambda(tokenizer, scope)
                    Kind.CLASS -> resolvedValue = resolveClass(tokenizer)
                    Kind.TRAIT -> resolvedValue = resolveTrait(tokenizer)
                    Kind.IMPL -> resolvedValue = resolveImpl(tokenizer)
                    Kind.UNPARSEABLE -> throw RuntimeException("Can't obtain value for unparseable definition.")
                    Kind.LOCAL_VARIABLE -> throw RuntimeException("Can't obtain local variable value from Definition.")
                }
            } catch (e: Exception) {
                throw exceptionInResolve(e, tokenizer)
            }
        }
        return resolvedValue
    }

    fun initializer(): Evaluable<RuntimeContext>? {
        if (kind != Kind.STATIC_VARIABLE && kind != Kind.LOCAL_VARIABLE) {
            throw IllegalStateException("Initilizer not available for $kind")
        }
        if (resolvedInitializer == UnresolvedEvalueable) {
            val tokenizer = tokenizer()
            try {
                resolveVariable(tokenizer)
            } catch (e: Exception) {
                throw exceptionInResolve(e, tokenizer)
            }
        }
        return resolvedInitializer
    }

    private fun resolveClass(tokenizer: TantillaTokenizer): Scope {
        println("Resolving class $name: $definitionText")
        val classContext = UserClassDefinition(name, scope)
        tokenizer.consume(":")
        Parser.parse(tokenizer, classContext)
        println("Class successfully resolved!")
        return classContext
    }

    private fun resolveTrait(tokenizer: TantillaTokenizer): Scope {
        println("Resolving trait $name: $definitionText")
        val traitContext = TraitDefinition(name, scope)
        tokenizer.consume(":")
        Parser.parse(tokenizer, traitContext)
        println("Trait successfully resolved!")
        return traitContext
    }

    private fun resolveImpl(tokenizer: TantillaTokenizer): Scope {
        println("Resolving impl $name: $definitionText")
        val traitName = name.substring(0, name.indexOf(' '))
        val trait = scope.resolveDynamic(traitName).value() as TraitDefinition
        val className = name.substring(name.lastIndexOf(' ') + 1)
        val implFor = scope.resolveDynamic(className).value() as UserClassDefinition
        val implContext = ImplDefinition(name, scope, trait, implFor)
        tokenizer.next()
        Parser.parse(tokenizer, implContext)
        println("Impl successfully resolved!")
        return implContext
    }

    private fun resolveVariable(tokenizer: TantillaTokenizer, typeOnly: Boolean = false) {
        if (tokenizer.tryConsume(":")) {
            resolvedType = Parser.parseType(tokenizer, scope)
            if (typeOnly) {
                return
            }
        }
        if (tokenizer.tryConsume("=")) {
            resolvedInitializer = Parser.parseExpression(tokenizer, scope)
            if (resolvedType == null) {
                resolvedType = resolvedInitializer!!.returnType
            } else if (!resolvedType!!.isAssignableFrom(resolvedInitializer!!.returnType)) {
                throw tokenizer.exception("Initializer type mismatch: ${resolvedInitializer!!.returnType} can't be assigned to $resolvedType")
            }
        } else if (resolvedType != null) {
            resolvedInitializer = null
        } else {
            throw tokenizer.exception("Explicit type or initializer expression required.")
        }
        if (kind == Kind.STATIC_VARIABLE) {
            resolvedValue = resolvedInitializer!!.eval(RuntimeContext(mutableListOf()))
        }
    }


    override fun toString() = serializeCode()


    fun serializeTitle(writer: CodeWriter) {
        when (kind) {
            Kind.STATIC_VARIABLE,
            Kind.LOCAL_VARIABLE -> writer.keyword(if (mutable) "var " else "val ")
                .declaration(name)
                //.append(if (explicitType != null) ": " + explicitType.typeName else "")
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
               if (resolvedInitializer != UnresolvedEvalueable) {
                   writer.append(": ")
                   writer.appendType(type())
                   if (resolvedInitializer != null) {
                       writer.append(" = ")
                       writer.appendCode(resolvedInitializer)
                   }
               } else {
                   writer.append(definitionText)
               }
           }
           Kind.UNPARSEABLE -> writer.append(definitionText)
           Kind.TRAIT,
           Kind.CLASS,
           Kind.IMPL -> {
               if (resolvedValue != UnresolvedValue) {
                   writer.appendCode(resolvedValue)
               } else {
                   serializeTitle(writer)
                   writer.append(definitionText)
               }
           }
           Kind.FUNCTION -> {
               writer.keyword("def ").declaration(name)
               if (resolvedValue != UnresolvedValue) {
                   writer.appendCode(resolvedValue)
               } else {
                    writer.append(definitionText)
               }
           }
       }
    }

    fun index() = scope.locals.indexOf(name)

    fun isDyanmic() = kind == Definition.Kind.LOCAL_VARIABLE
            || (kind == Definition.Kind.FUNCTION && (type() as FunctionType).isMethod())

    fun isStatic() = !isDyanmic()

    fun isScope() = error() == null &&
            (kind == Definition.Kind.IMPL || kind == Definition.Kind.CLASS || kind == Definition.Kind.TRAIT)

    object UnresolvedEvalueable: TantillaNode {
        override fun children(): List<Evaluable<RuntimeContext>> {
            TODO("Not yet implemented")
        }

        override fun eval(context: RuntimeContext): Any? {
            TODO("Not yet implemented")
        }

        override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>): Evaluable<RuntimeContext> {
            TODO("Not yet implemented")
        }

        override fun serializeCode(writer: CodeWriter, precedence: Int) {
            TODO("Not yet implemented")
        }

        override val returnType: Type
            get() = TODO("Not yet implemented")
    }


    object UnresolvedValue
}