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
                when (kind) {
                    Definition.Kind.FUNCTION -> resolvedType = resolveFunctionType(tokenizer, isMethod = false)
                    Definition.Kind.METHOD -> resolvedType = resolveFunctionType(tokenizer, isMethod = true)
                    Definition.Kind.FIELD,
                    Definition.Kind.STATIC -> resolveVariable(tokenizer, typeOnly = true)
                    else -> resolvedType = value().dynamicType
                }
            } catch (e: Exception) {
                throw exceptionInResolve(e, tokenizer)
            }
        }
        return resolvedType!!
    }

    private fun resolveFunctionType(tokenizer: TantillaTokenizer, isMethod: Boolean): FunctionType {
        tokenizer.tryConsume("static")
        tokenizer.consume("def")
        tokenizer.consume(name)
        return TypeParser.parseFunctionType(tokenizer, ParsingContext(parentScope, 0), isMethod)
    }

    override fun value(): Any?  {
        if (resolvedValue == UnresolvedValue) {
            val tokenizer = tokenizer()
            println("Resolving: $definitionText")
            try {
                when (kind) {
                    Definition.Kind.STATIC -> resolveVariable(tokenizer)
                    Definition.Kind.FUNCTION -> {
                        val resolved = Parser.parseDef(tokenizer, ParsingContext(parentScope, 0), isMethod = false)
                        docString = resolved.first
                        resolvedValue = resolved.second
                    }
                    Definition.Kind.METHOD -> {
                        val resolved = Parser.parseDef(tokenizer, ParsingContext(parentScope, 0), isMethod = true)
                        docString = resolved.first
                        resolvedValue = resolved.second
                    }
                    Definition.Kind.STRUCT -> resolvedValue = resolveClass(tokenizer)
                    Definition.Kind.TRAIT -> resolvedValue = resolveTrait(tokenizer)
                    Definition.Kind.IMPL -> resolvedValue = resolveImpl(tokenizer)
                    Definition.Kind.UNPARSEABLE -> throw RuntimeException("Can't obtain value for unparseable definition.")
                    Definition.Kind.FIELD -> throw RuntimeException("Can't obtain local variable value from Definition.")
                    Definition.Kind.UNIT -> throw UnsupportedOperationException()
                }
            } catch (e: Exception) {
                throw exceptionInResolve(e, tokenizer)
            }
        }
        return resolvedValue
    }

    override fun initializer(): Evaluable<RuntimeContext>? {
        if (kind != Definition.Kind.STATIC && kind != Definition.Kind.FIELD) {
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
        when (kind) {
            Definition.Kind.STATIC,
            Definition.Kind.FIELD -> {
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
            Definition.Kind.METHOD  -> writer.keyword("def ").declaration(name).appendType(valueType())
            Definition.Kind.FUNCTION -> {
                if (parentScope.supportsMethods) {
                    writer.keyword("static ")
                }
                writer.keyword("def ").declaration(name).appendType(valueType())
            }
            Definition.Kind.TRAIT,
            Definition.Kind.IMPL,
            Definition.Kind.STRUCT -> writer.keyword(kind.name.lowercase()).append(' ').declaration(name)
            Definition.Kind.UNPARSEABLE -> writer.append("(unparseable: $name)")
            Definition.Kind.UNIT -> writer.keyword("unit ").declaration(name)
        }
    }


    override fun serializeCode(writer: CodeWriter, precedence: Int) {
       when (kind) {
           Definition.Kind.STATIC,
           Definition.Kind.FIELD -> {
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
           Definition.Kind.UNPARSEABLE -> writer.append(definitionText)
           Definition.Kind.TRAIT,
           Definition.Kind.STRUCT,
           Definition.Kind.IMPL -> {
               if (resolvedValue != UnresolvedValue) {
                   writer.appendCode(resolvedValue)
               } else {
                   writer.append(definitionText)
               }
           }
           Definition.Kind.METHOD,
           Definition.Kind.FUNCTION -> {
               if (resolvedValue != UnresolvedValue) {
                   writer.keyword("def ").declaration(name)
                   writer.appendCode(resolvedValue)
               } else {
                    writer.append(definitionText)
               }
           }
       }
    }

    override fun serializeSummary(writer: CodeWriter) {
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


    override fun isDynamic() = kind == Definition.Kind.FIELD || kind == Definition.Kind.METHOD

    override fun isScope() = error() == null && (
            kind == Definition.Kind.IMPL
            || kind == Definition.Kind.STRUCT
            || kind == Definition.Kind.TRAIT || kind == Definition.Kind.UNIT)

    override fun findNode(node: Evaluable<RuntimeContext>): Definition? {
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