package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.classifier.UserClassDefinition
import org.kobjects.tantilla2.core.classifier.ImplDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.LambdaImpl
import org.kobjects.tantilla2.core.node.TantillaNode
import org.kobjects.tantilla2.core.node.containsNode
import org.kobjects.tantilla2.core.parser.*

class Definition(
    val scope: Scope,
    val kind: Kind,
    val name: String,
    val definitionText: String = "",
    val mutable: Boolean = false,
    private var resolvedType: Type? = null,
    private var resolvedValue: Any? = UnresolvedValue,
    var docString: String = "",
    var index: Int = -1,
) : SerializableCode {
    var error: Exception? = null

    private var resolvedInitializer: Evaluable<RuntimeContext>? = UnresolvedEvalueable

    enum class Kind {
        FIELD, STATIC, FUNCTION, METHOD, TRAIT, STRUCT, UNIT, IMPL, UNPARSEABLE
    }

    init {
        if (kind == Kind.FIELD) {
            val existingIndex = scope.locals.indexOf(name)
            if (index != existingIndex) {
                throw IllegalArgumentException("local variable inconsistency new index: $index; existing: $existingIndex")
            }
        } else if (index != -1) {
            throw IllegalArgumentException("index must be -1 for $kind")
        }
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
            error = ParsingException(tokenizer.current, "Error in ${scope.title}.$name: " +  (e.message ?: "Parsing Error"), e)
        }
        error!!.printStackTrace()
        throw error!!
    }

    fun error(): Exception? {
        if (error == null) {
            try {
                when (kind) {
                    Kind.FIELD  -> initializer()
                    Kind.UNPARSEABLE -> null
                    else -> value()
                }
            } catch (e: Exception) {
                println("Error in $scope.$name")
                e.printStackTrace()
            }
        }
        return error;
    }

    fun rebuild(compilationResults: CompilationResults): Boolean {
        var ok = true
        if (error() != null) {
            compilationResults.errors.add(this)
            ok = false
        }
        if (isScope()) {
            val value = value() as Scope
            if (!value.rebuild(compilationResults)) {
                compilationResults.errors.add(this)
                ok = false
            } else if (value is ImplDefinition) {

                compilationResults.classToTrait.getOrPut(value.classifier) { mutableMapOf() }[value.trait] = this
                compilationResults.traitToClass.getOrPut(value.trait) { mutableMapOf() }[value.classifier] = this

            }
        }
        return ok
    }


    fun type(): Type {
        if (resolvedType == null) {
            val tokenizer = tokenizer()
            try {
                when (kind) {
                    Kind.FUNCTION -> resolvedType = resolveFunctionType(tokenizer, isMethod = false)
                    Kind.METHOD -> resolvedType = resolveFunctionType(tokenizer, isMethod = true)
                    Kind.FIELD,
                    Kind.STATIC -> resolveVariable(tokenizer, typeOnly = true)
                    else -> resolvedType = value().dynamicType
                }
            } catch (e: Exception) {
                throw exceptionInResolve(e, tokenizer)
            }
        }
        return resolvedType!!
    }

    fun resolveFunctionType(tokenizer: TantillaTokenizer, isMethod: Boolean): FunctionType {
        tokenizer.tryConsume("static")
        tokenizer.consume("def")
        tokenizer.consume(name)
        return TypeParser.parseFunctionType(tokenizer, ParsingContext(scope, 0), isMethod)
    }

    fun value(): Any?  {
        if (resolvedValue == UnresolvedValue) {
            val tokenizer = tokenizer()
            println("Resolving: $definitionText")
            try {
                when (kind) {
                    Kind.STATIC -> resolveVariable(tokenizer)
                    Kind.FUNCTION -> {
                        val resolved = Parser.parseDef(tokenizer, ParsingContext(scope, 0), isMethod = false)
                        docString = resolved.first
                        resolvedValue = resolved.second
                    }
                    Kind.METHOD -> {
                        val resolved = Parser.parseDef(tokenizer, ParsingContext(scope, 0), isMethod = true)
                        docString = resolved.first
                        resolvedValue = resolved.second
                    }
                    Kind.STRUCT -> resolvedValue = resolveClass(tokenizer)
                    Kind.TRAIT -> resolvedValue = resolveTrait(tokenizer)
                    Kind.IMPL -> resolvedValue = resolveImpl(tokenizer)
                    Kind.UNPARSEABLE -> throw RuntimeException("Can't obtain value for unparseable definition.")
                    Kind.FIELD -> throw RuntimeException("Can't obtain local variable value from Definition.")
                    Kind.UNIT -> throw UnsupportedOperationException()
                }
            } catch (e: Exception) {
                throw exceptionInResolve(e, tokenizer)
            }
        }
        return resolvedValue
    }

    fun initializer(): Evaluable<RuntimeContext>? {
        if (kind != Kind.STATIC && kind != Kind.FIELD) {
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
        val classContext = UserClassDefinition(name, scope)
        tokenizer.consume("struct")
        tokenizer.consume(name)
        tokenizer.consume(":")
        Parser.parse(tokenizer, ParsingContext(classContext, 1))
        println("Class successfully resolved!")
        return classContext
    }

    private fun resolveTrait(tokenizer: TantillaTokenizer): Scope {
        val traitContext = TraitDefinition(name, scope)
        tokenizer.consume("trait")
        tokenizer.consume(name)
        tokenizer.consume(":")
        Parser.parse(tokenizer, ParsingContext(traitContext, 1))
        println("Trait successfully resolved!")
        return traitContext
    }

    private fun resolveImpl(tokenizer: TantillaTokenizer): Scope {
        val traitName = name.substring(0, name.indexOf(' '))
        val trait = scope.resolveStatic(traitName, true)!!.value() as TraitDefinition
        val className = name.substring(name.lastIndexOf(' ') + 1)
        val implFor = scope.resolveStatic(className, true)!!.value() as UserClassDefinition
        val implContext = ImplDefinition(name, scope, trait, implFor)
        tokenizer.consume("impl")
        tokenizer.consume(traitName)
        tokenizer.consume("for")
        tokenizer.consume(className)
        tokenizer.consume(":")
        Parser.parse(tokenizer, ParsingContext(implContext, 0))
        println("Impl successfully resolved!")
        return implContext
    }

    private fun resolveVariable(tokenizer: TantillaTokenizer, typeOnly: Boolean = false) {
        if (definitionText.isEmpty()) {
            resolvedType = resolvedValue.dynamicType
            return
        }

        tokenizer.tryConsume("static")
        tokenizer.tryConsume("mut")
        tokenizer.tryConsume("var") || tokenizer.tryConsume("val") // var/val

        tokenizer.consume(name)

        val resolved = Parser.resolveVariable(tokenizer, ParsingContext(scope, 0))
        resolvedType = resolved.first

        resolvedInitializer = resolved.third

        if (kind == Definition.Kind.STATIC) {
            resolvedValue = resolvedInitializer!!.eval(RuntimeContext(mutableListOf()))
        }
    }


    override fun toString() = serializeCode()


    fun serializeTitle(writer: CodeWriter) {
        when (kind) {
            Kind.STATIC,
            Kind.FIELD -> {
                if (kind == Kind.STATIC && scope.supportsLocalVariables) {
                    writer.keyword("static ")
                }
                if (mutable) {
                    writer.keyword("mut ")
                }
                writer.declaration(name)
                writer.append(": ")
                writer.appendType(type())
            }
            Kind.METHOD  -> writer.keyword("def ").declaration(name).appendType(type())
            Kind.FUNCTION -> {
                if (scope.supportsMethods) {
                    writer.keyword("static ")
                }
                writer.keyword("def ").declaration(name).appendType(type())
            }
            Kind.TRAIT,
            Kind.IMPL,
            Kind.STRUCT -> writer.keyword(kind.name.lowercase()).append(' ').declaration(name)
            Kind.UNPARSEABLE -> writer.append("(unparseable: $name)")
            Kind.UNIT -> writer.keyword("unit ").declaration(name)
        }
    }


    override fun serializeCode(writer: CodeWriter, precedence: Int) {
       when (kind) {
           Kind.STATIC,
           Kind.FIELD -> {
               if (resolvedInitializer != UnresolvedEvalueable) {
                   serializeTitle(writer)
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
           Kind.STRUCT,
           Kind.IMPL -> {
               if (resolvedValue != UnresolvedValue) {
                   writer.appendCode(resolvedValue)
               } else {
                   writer.append(definitionText)
               }
           }
           Kind.METHOD,
           Kind.FUNCTION -> {
               if (resolvedValue != UnresolvedValue) {
                   writer.keyword("def ").declaration(name)
                   writer.appendCode(resolvedValue)
               } else {
                    writer.append(definitionText)
               }
           }
       }
    }

    fun serializeSummaray(writer: CodeWriter) {
        if (!isScope()) {
            serializeCode(writer)
            return
        }
        serializeTitle(writer)
        writer.append(":")
        writer.indent()
        val scope = value() as Scope
        for (definition in scope.iterator()) {
            writer.newline()
            definition.serializeTitle(writer)
        }
        writer.outdent()
    }


    fun isDyanmic() = kind == Definition.Kind.FIELD || kind == Definition.Kind.METHOD

    fun isStatic() = !isDyanmic()

    fun isScope() = error() == null && (
            kind == Definition.Kind.IMPL
            || kind == Definition.Kind.STRUCT
            || kind == Definition.Kind.TRAIT || kind == Definition.Kind.UNIT)

    fun findNode(node: Evaluable<RuntimeContext>): Definition? {
        val rid = resolvedInitializer
        if (rid != UnresolvedEvalueable && rid != null && rid.containsNode(node)) {
            return this
        }
        return when (val value = resolvedValue) {
            is LambdaImpl -> if (value.body.containsNode(node)) this else null
            is Scope -> value.findNode(node)
            else -> null
        }
    }

    fun depth(scope: Scope): Int {
        if (scope == this.scope) {
            return 0
        }
        if (scope.parentContext == null) {
            throw IllegalStateException("Definition $this not found in scope.")
        }
        return 1 + depth(scope.parentContext!!)
    }

    object UnresolvedEvalueable: TantillaNode {
        override fun children() = emptyList<Evaluable<RuntimeContext>>()

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