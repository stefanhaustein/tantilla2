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

class VariableDefinition (
    override val parentScope: Scope,
    override val kind: Definition.Kind,
    override val name: String,
    val definitionText: String = "",
    override val mutable: Boolean = false,
    private var resolvedType: Type? = null,
    private var resolvedValue: Any? = UnresolvedValue,
    override var docString: String = "",
    override var index: Int = -1,
) : Definition {
    var error: Exception? = null

    private var resolvedInitializer: Evaluable<RuntimeContext>? = UnresolvedEvalueable

    init {
        when (kind) {
            Definition.Kind.FIELD -> {
                val existingIndex = parentScope.locals.indexOf(name)
                if (index != existingIndex) {
                    throw IllegalArgumentException("local variable inconsistency new index: $index; existing: $existingIndex")
                }
            }
            Definition.Kind.STATIC -> {
                if (index != -1) {
                    throw IllegalArgumentException("index must be -1 for $kind")
                }
            }
            else -> IllegalArgumentException("Kind must be FIELD or STATIC for VariableDefinition")
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
            error = ParsingException(tokenizer.current, "Error in ${parentScope.title}.$name: " +  (e.message ?: "Parsing Error"), e)
        }
        error!!.printStackTrace()
        throw error!!
    }

    override fun error(): Exception? {
        if (error == null) {
            try {
                when (kind) {
                    Definition.Kind.FIELD  -> initializer()
                    Definition.Kind.UNPARSEABLE -> null
                    else -> value()
                }
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
        return ok
    }


    override fun valueType(): Type {
        if (resolvedType == null) {
            val tokenizer = tokenizer()
            try {
                resolveVariable(tokenizer, typeOnly = true)
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
                    Definition.Kind.STATIC -> resolveVariable(tokenizer)
                    else -> throw RuntimeException("Can't obtain value for $kind.")
                }
            } catch (e: Exception) {
                throw exceptionInResolve(e, tokenizer)
            }
        }
        return resolvedValue
    }

    override fun initializer(): Evaluable<RuntimeContext>? {
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

    private fun resolveVariable(tokenizer: TantillaTokenizer, typeOnly: Boolean = false) {
        if (definitionText.isEmpty()) {
            resolvedType = resolvedValue.dynamicType
            return
        }

        tokenizer.tryConsume("static")
        tokenizer.tryConsume("mut")
        tokenizer.tryConsume("var") || tokenizer.tryConsume("val") // var/val

        tokenizer.consume(name)

        val resolved = Parser.resolveVariable(tokenizer, ParsingContext(parentScope, 0))
        resolvedType = resolved.first

        resolvedInitializer = resolved.third

        if (kind == Definition.Kind.STATIC) {
            resolvedValue = resolvedInitializer!!.eval(RuntimeContext(mutableListOf()))
        }
    }


    override fun toString() = serializeCode()


    override fun serializeTitle(writer: CodeWriter) {
        if (kind == Definition.Kind.STATIC && parentScope.supportsLocalVariables) {
            writer.keyword("static ")
        }
        if (mutable) {
            writer.keyword("mut ")
        }
        writer.declaration(name)
        writer.append(": ")
        writer.appendType(valueType())
    }


    override fun serializeCode(writer: CodeWriter, precedence: Int) {

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

    override fun serializeSummary(writer: CodeWriter) {
        serializeCode(writer)
    }


    override fun isDynamic() = kind == Definition.Kind.FIELD

    override fun isScope() = false

    override fun findNode(node: Evaluable<RuntimeContext>): Definition? {
        val rid = resolvedInitializer
        if (rid != UnresolvedEvalueable && rid != null && rid.containsNode(node)) {
            return this
        }
        return null
    }

    override fun depth(scope: Scope): Int {
        if (scope == this.parentScope) {
            return 0
        }
        if (scope.parentScope == null) {
            throw IllegalStateException("Definition $this not found in scope.")
        }
        return 1 + depth(scope.parentScope!!)
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