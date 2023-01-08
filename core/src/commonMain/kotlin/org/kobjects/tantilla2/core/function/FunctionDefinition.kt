package org.kobjects.tantilla2.core.function

import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.definition.DefinitionUpdatable
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.node.expression.UnresolvedNode
import org.kobjects.tantilla2.core.node.statement.FlowSignal
import org.kobjects.tantilla2.core.parser.*
import org.kobjects.tantilla2.core.type.UnresolvedType

class FunctionDefinition (
    override val parentScope: Scope,
    override val kind: Definition.Kind,
    override val name: String,
    definitionText: String,
) : Scope(), Callable, DefinitionUpdatable {
    override var docString: String = ""

    private var resolvedType: FunctionType = UnresolvedType
    private var resolvedBody: Node = UnresolvedNode

    var _definitionText = definitionText
    override var definitionText: String
        get() = _definitionText
        set(value) {
            reset()
            _definitionText = value
        }

    init {
        if (kind != Definition.Kind.FUNCTION && kind != Definition.Kind.METHOD) {
            throw IllegalArgumentException("Variable definition ($kind) not supported in DefinitionImpl.")
        }
    }

    override val supportsLocalVariables: Boolean
        get() = true

    override val dynamicScopeSize: Int
        get() = if (parentScope is TraitDefinition) super.dynamicScopeSize
            else {
                resolve()
                locals.size
            }

    override val type: FunctionType
        get() {
            resolve(typeOnly = true)
            return resolvedType
        }

    override fun getValue(self: Any?): FunctionDefinition {
        return this
    }

    override fun resolve() {
        resolve(false)
    }

    fun body(): Node {
        resolve()
        return resolvedBody!!
    }

    private fun resolve(typeOnly: Boolean) {
        if (resolvedBody != UnresolvedNode
            || (typeOnly && resolvedType != UnresolvedType)) {
            return
        }

        val tokenizer = TantillaTokenizer(definitionText)
        try {
            tokenizer.consume(TokenType.BOF)

            tokenizer.tryConsume("static")
            tokenizer.consume("def")
            tokenizer.consume(TokenType.IDENTIFIER)
            resolvedType = TypeParser.parseFunctionType(
                tokenizer,
                ParsingContext(parentScope, 0),
                kind == Definition.Kind.METHOD
            )

            if (typeOnly) {
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
                resolvedBody =
                    Parser.parseDefinitionsAndStatements(tokenizer, 1, this, definitionScope = this)
            }
        } catch (e: Exception) {
            if (e is ParsingException) {
                throw e
            } else {
                throw tokenizer.exception(e.toString())
            }
        }
    }

    override fun eval(context: LocalRuntimeContext): Any {
        resolve()
        val result = resolvedBody!!.eval(context)
        if (result is FlowSignal) {
            if (result.kind == FlowSignal.Kind.RETURN) {
                return result.value
            }
            throw IllegalStateException("Unexpected signal: $result")
        }
        return result
    }

    // Avoid calling serialization here, as this might lead to recursive crashes in error
    // reporting.
    override fun toString() = "def $name"

    override fun isSummaryExpandable() = true

    override fun serializeSummary(writer: CodeWriter, length: Definition.SummaryKind) {
        if (length == Definition.SummaryKind.EXPANDED) {
            serializeCode(writer)
        } else if (resolvedType == UnresolvedType) {
            writer.appendUnparsed(definitionText.split('\n').first(), userRootScope().definitionsWithErrors[this] ?: emptyList())
        } else {
            if (parentScope.supportsMethods && kind == Definition.Kind.FUNCTION) {
                writer.appendKeyword("static ")
            }
            writer.appendKeyword("def ").appendDeclaration(name)
            type.serializeType(writer)
        }
    }

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        if (resolvedBody == UnresolvedNode) {
            writer.appendUnparsed(definitionText, userRootScope().definitionsWithErrors[this] ?: emptyList())
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

    override fun isDynamic() = kind == Definition.Kind.METHOD

    override fun findNode(node: Node): Definition? =
        if (resolvedBody?.containsNode(node) ?: false) this else null

    override fun reset() {
        super.reset()
        resolvedType = UnresolvedType
        resolvedBody = UnresolvedNode
        locals.clear()
    }
}