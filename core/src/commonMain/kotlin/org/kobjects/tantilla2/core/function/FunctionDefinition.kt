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
) : Scope(), Callable {
    override var docString: String = ""

    private var resolutionState: ResolutionState = ResolutionState.UNRESOLVED
    private var resolvedType: FunctionType? = null
    internal var resolvedBody: Evaluable<RuntimeContext>? = null

    init {
        if (kind != Definition.Kind.FUNCTION && kind != Definition.Kind.METHOD) {
            throw IllegalArgumentException("Variable definition ($kind) not supported in DefinitionImpl.")
        }
    }

    override val supportsLocalVariables: Boolean
        get() = true

    override val scopeSize: Int
        get() = if (parentScope is TraitDefinition) super.scopeSize
            else getValue(null).definitions.locals.size

    override val errors: List<Exception>
        get() {
            try {
                resolve()
            } catch (e: Exception) {
                return listOf(e)
            }
            return emptyList()
    }

    override val type: FunctionType
        get() {
            resolve(typeOnly = true)
            return resolvedType!!
        }

    override fun getValue(self: Any?): FunctionDefinition {
        resolve()
        return this
    }

    private fun resolve(typeOnly: Boolean = false) {
        when (resolutionState) {
            ResolutionState.RESOLVED -> return
            ResolutionState.TYPE_RESOLVED -> if (typeOnly) return
          /*  ResolutionState.ERROR -> {
                throw error!!
            } */
        }

        val tokenizer = TantillaTokenizer(definitionText)
        tokenizer.consume(TokenType.BOF)
        error = null

        try {
           tokenizer.tryConsume("static")
           tokenizer.consume("def")
           tokenizer.consume(TokenType.IDENTIFIER)
           resolvedType = TypeParser.parseFunctionType(
               tokenizer,
               ParsingContext(parentScope, 0),
               kind == Definition.Kind.METHOD
           )

           if (typeOnly) {
               resolutionState = ResolutionState.TYPE_RESOLVED
               return
           }

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

           resolutionState = ResolutionState.RESOLVED
       } catch (e: Exception) {
           if (e is ParsingException) {
               error = e
           } else {
               error = ParsingException(tokenizer.current, "Error in ${parentScope.name}.$name: " +  (e.message ?: "Parsing Error"), e)
           }
           error!!.printStackTrace()
           resolutionState = ResolutionState.ERROR
           throw error!!
       }
    }

    override fun eval(context: RuntimeContext): Any? {
        resolve()
        val result = resolvedBody!!.eval(context)
        if (result is Control.FlowSignal) {
            if (result.kind == Control.FlowSignal.Kind.RETURN) {
                return result.value
            }
            throw IllegalStateException("Unexpected signal: $result")
        }
        return result
    }

    override fun toString() = serializeCode()

    override fun serializeTitle(writer: CodeWriter, abbreviated: Boolean) {
                if (parentScope.supportsMethods && kind == Definition.Kind.FUNCTION) {
                    writer.appendKeyword("static ")
                }
                writer.appendKeyword("def ").appendDeclaration(name)
        if (abbreviated) {
            type.serializeAbbreviatedType(writer)
        } else {
            type.serializeType(writer)
        }
    }


    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        if (resolvedBody == null) {
            writer.append(definitionText)
        } else {
            if (parentScope.supportsMethods && !isDynamic()) {
                writer.appendKeyword("static ")
            }
            writer.appendKeyword("def ").appendDeclaration(name).appendType(type)
            if (resolvedBody !is TraitMethodBody) {
                writer.append(":")
                writer.indent()
                writer.newline()
                if (docString.isNotEmpty()) {
                    writer.appendTripleQuoted(docString)
                    writer.newline()
                }

                writer.appendCode(resolvedBody)
                writer.outdent()
            }
        }
    }

    override fun serializeSummary(writer: CodeWriter) {
        serializeCode(writer)
    }


    override fun isDynamic() = kind == Definition.Kind.METHOD

    override fun isScope() = false

    override fun findNode(node: Evaluable<RuntimeContext>): Definition? =
        if (resolvedBody?.containsNode(node) ?: false) this else null

    override fun reset() {
        resolutionState = ResolutionState.UNRESOLVED
        resolvedType = null
        resolvedBody = null
        super.reset()
    }

    enum class ResolutionState {
        UNRESOLVED, TYPE_RESOLVED, RESOLVED, ERROR
    }
}