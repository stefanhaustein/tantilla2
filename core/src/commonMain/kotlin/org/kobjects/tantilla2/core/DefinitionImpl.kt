package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.classifier.UserClassDefinition
import org.kobjects.tantilla2.core.classifier.ImplDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.parser.*

class DefinitionImpl (
    override val parentScope: Scope,
    override val kind: Definition.Kind,
    override val name: String,
    val definitionText: String = "",
    private var resolvedType: Type? = null,
    private var resolvedValue: Any? = UnresolvedValue,
    override var docString: String = "",
) : Definition {
    var error: Exception? = null

    init {
        if (kind != Definition.Kind.UNIT
            && kind != Definition.Kind.STATIC
            && kind != Definition.Kind.TRAIT
            && kind != Definition.Kind.STRUCT
            && kind != Definition.Kind.IMPL) {
            throw IllegalArgumentException("$kind not supported in DefinitionImpl.")
        }
    }

    override val mutable: Boolean
        get() = false

    override var index: Int
        get() = -1
        set(_) = throw UnsupportedOperationException()

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
            error = ParsingException(tokenizer.current, "Error in ${parentScope.name}.$name: " +  (e.message ?: "Parsing Error"), e)
        }
        error!!.printStackTrace()
        throw error!!
    }

    override fun error(): Exception? {
        if (error == null) {
            try {
                value()
            } catch (e: Exception) {
                println("Error in $parentScope.$name")
                e.printStackTrace()
            }
        }
        return error;
    }

    override fun rebuild(compilationResults: CompilationResults): Boolean {
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


    override fun valueType(): Type {
        if (resolvedType == null) {
            val tokenizer = tokenizer()
            try {
                resolvedType = value().dynamicType
            } catch (e: Exception) {
                throw exceptionInResolve(e, tokenizer)
            }
        }
        return resolvedType!!
    }

    override fun value(): Any?  {
        if (resolvedValue == UnresolvedValue) {
            val tokenizer = tokenizer()
            println("Resolving: $definitionText")
            try {
                when (kind) {
                    Definition.Kind.STRUCT -> resolvedValue = resolveClass(tokenizer)
                    Definition.Kind.TRAIT -> resolvedValue = resolveTrait(tokenizer)
                    Definition.Kind.IMPL -> resolvedValue = resolveImpl(tokenizer)
                    Definition.Kind.UNIT -> throw UnsupportedOperationException()
                    else -> throw IllegalStateException("Not supported here: $kind")
                }
            } catch (e: Exception) {
                throw exceptionInResolve(e, tokenizer)
            }
        }
        return resolvedValue
    }

    override fun initializer(): Evaluable<RuntimeContext>? {
        throw IllegalStateException("Initilizer not available for $kind")
    }

    private fun resolveClass(tokenizer: TantillaTokenizer): Scope {
        val classContext = UserClassDefinition(name, parentScope)
        tokenizer.consume("struct")
        tokenizer.consume(name)
        tokenizer.consume(":")
        Parser.parse(tokenizer, ParsingContext(classContext, 1))
        println("Class successfully resolved!")
        return classContext
    }

    private fun resolveTrait(tokenizer: TantillaTokenizer): Scope {
        val traitContext = TraitDefinition(name, parentScope)
        tokenizer.consume("trait")
        tokenizer.consume(name)
        tokenizer.consume(":")
        Parser.parse(tokenizer, ParsingContext(traitContext, 1))
        println("Trait successfully resolved!")
        return traitContext
    }

    private fun resolveImpl(tokenizer: TantillaTokenizer): Scope {
        val traitName = name.substring(0, name.indexOf(' '))
        val trait = parentScope.resolveStatic(traitName, true)!!.value() as TraitDefinition
        val className = name.substring(name.lastIndexOf(' ') + 1)
        val implFor = parentScope.resolveStatic(className, true)!!.value() as UserClassDefinition
        val implContext = ImplDefinition(name, parentScope, trait, implFor)
        tokenizer.consume("impl")
        tokenizer.consume(traitName)
        tokenizer.consume("for")
        tokenizer.consume(className)
        tokenizer.consume(":")
        Parser.parse(tokenizer, ParsingContext(implContext, 0))
        println("Impl successfully resolved!")
        return implContext
    }



    override fun toString() = serializeCode()


    override fun serializeTitle(writer: CodeWriter) {
        writer.keyword(kind.name.lowercase()).append(' ').declaration(name)
    }


    override fun serializeCode(writer: CodeWriter, precedence: Int) {

               if (resolvedValue != UnresolvedValue) {
                   writer.appendCode(resolvedValue)
               } else {
                   writer.append(definitionText)
               }

    }

    override fun serializeSummary(writer: CodeWriter) {
        serializeTitle(writer)
        writer.append(":")
        writer.indent()
        val scope = value() as Scope
        for (definition in scope.definitions.iterator()) {
            writer.newline()
            definition.serializeTitle(writer)
        }
        writer.outdent()
    }

    override fun isDynamic() = false

    override fun isScope() = error() == null

    override fun findNode(node: Evaluable<RuntimeContext>): Definition? =
        when (val value = resolvedValue) {
            is Scope -> value.findNode(node)
            else -> null
        }

    override fun depth(scope: Scope): Int {
        if (scope == this.parentScope) {
            return 0
        }
        if (scope.parentScope == null) {
            throw IllegalStateException("Definition $this not found in scope.")
        }
        return 1 + depth(scope.parentScope)
    }


    object UnresolvedValue
}