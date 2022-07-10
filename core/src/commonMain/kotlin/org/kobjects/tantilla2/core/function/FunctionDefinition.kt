package org.kobjects.tantilla2.core.function

import org.kobjects.greenspun.core.Control
import org.kobjects.greenspun.core.Evaluable
import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.node.containsNode
import org.kobjects.tantilla2.core.parser.*

class FunctionDefinition (
    override val parentScope: Scope,
    override val kind: Definition.Kind,
    override val name: String,
    val definitionText: String,
    private var resolvedType: FunctionType? = null
) : Scope(), Callable {

    override var docString: String = ""

    internal var resolvedBody: Evaluable<RuntimeContext>? = null

    init {
        if (kind != Definition.Kind.FUNCTION && kind != Definition.Kind.METHOD) {
            throw IllegalArgumentException("Variable definition ($kind) not supported in DefinitionImpl.")
        }
    }

  /*  override val supportsMethods: Boolean
        get() = true
*/
    override val supportsLocalVariables: Boolean
        get() = true

    override val scopeSize: Int
        get() = if (definitionText.isEmpty() || parentScope is TraitDefinition) super.scopeSize else value.definitions.locals.size

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
                value
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


    override val type: FunctionType
        get() {
            if (resolvedType == null) {
                val tokenizer = tokenizer()
                try {
                    resolvedType = resolveFunctionType(tokenizer, isMethod = kind == Definition.Kind.METHOD)
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

    override val value: FunctionDefinition
        get() {
            if (resolvedBody == null) {
                val tokenizer = tokenizer()
                println("Resolving: $definitionText")
                try {
                    resolve()
                } catch (e: Exception) {
                    throw exceptionInResolve(e, tokenizer)
                }
            }
            return this
        }

    private fun resolve() {
        val tokenizer = tokenizer()
        tokenizer.tryConsume("static")
        tokenizer.consume("def")
        tokenizer.consume(TokenType.IDENTIFIER)
        val type = TypeParser.parseFunctionType(tokenizer, ParsingContext(parentScope, 0), kind == Definition.Kind.METHOD)
        for (parameter in type.parameters) {
            declareLocalVariable(parameter.name, parameter.type, false)
        }
        if (parentScope is TraitDefinition) {
            if (tokenizer.tryConsume(":")) {
                docString = Parser.readDocString(tokenizer)
            }
            tokenizer.consume(TokenType.EOF, "Trait methods must not have function bodies.")
            resolvedBody = TraitMethodBody(parentScope.traitIndex++)
        } else {
            tokenizer.consume(":")
            docString = Parser.readDocString(tokenizer)
            resolvedBody = Parser.parse(tokenizer, ParsingContext(this, 1))
        }
    }

    override fun eval(context: RuntimeContext): Any? {
        val result = value.resolvedBody!!.eval(context)
        if (result is Control.FlowSignal) {
            if (result.kind == Control.FlowSignal.Kind.RETURN) {
                return result.value
            }
            throw IllegalStateException("Unexpected signal: $result")
        }
        return result
    }



    override fun toString() = serializeCode()

    override fun serializeTitle(writer: CodeWriter) {
                if (parentScope.supportsMethods && kind == Definition.Kind.FUNCTION) {
                    writer.keyword("static ")
                }
                writer.keyword("def ").declaration(name).appendType(type)
    }


    override fun serializeCode(writer: CodeWriter, precedence: Int) {

               if (resolvedBody == null) {
                   writer.append(definitionText)
               } else {
                   writer.keyword("def ").declaration(name).appendType(type).append(":")
                   writer.indent()
                   writer.newline()
                   writer.appendCode(resolvedBody)
                   writer.outdent()
               }
    }

    override fun serializeSummary(writer: CodeWriter) {
            serializeCode(writer)
    }


    override fun isDynamic() = kind == Definition.Kind.METHOD

    override fun isScope() = false

    override fun findNode(node: Evaluable<RuntimeContext>): Definition? =
        if (resolvedBody?.containsNode(node) ?: false) this else null


}