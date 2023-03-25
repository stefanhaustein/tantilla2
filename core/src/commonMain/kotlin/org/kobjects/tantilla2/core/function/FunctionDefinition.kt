package org.kobjects.tantilla2.core.function

import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.definition.CodeFragment
import org.kobjects.tantilla2.core.definition.DefinitionUpdatable
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.scope.Scope
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.node.expression.UnresolvedNode
import org.kobjects.tantilla2.core.control.ReturnSignal
import org.kobjects.tantilla2.core.parser.*
import org.kobjects.tantilla2.core.type.UnresolvedType

class FunctionDefinition (
    override val parentScope: Scope,
    override val kind: Definition.Kind,
    override val name: String,
    definitionText: CodeFragment,
) : Scope(), Callable, DefinitionUpdatable {
    override var docString: String = ""

    private var resolvedType: FunctionType = UnresolvedType
    private var resolvedBody: Node = UnresolvedNode

    var _definitionText = definitionText
    override var definitionText: CodeFragment
        get() = _definitionText
        set(value) {
            invalidate()
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
            resolveImpl(applyOffset = false, typeOnly = true, errorCollector = null)
            return resolvedType
        }

    override fun getValue(self: Any?): FunctionDefinition {
        return this
    }

    override fun resolve(applyOffset: Boolean, errorCollector: MutableList<ParsingException>?) {
        resolveImpl(applyOffset = applyOffset, typeOnly = false,  errorCollector = errorCollector)
    }

    fun body(): Node {
        resolve()
        return resolvedBody!!
    }

    private fun resolveImpl(applyOffset: Boolean, typeOnly: Boolean, errorCollector: MutableList<ParsingException>?) {
        if (resolvedBody != UnresolvedNode
            || (typeOnly && resolvedType != UnresolvedType)) {
            return
        }


        if (applyOffset) {
            println("######### Creating tokenizer with '${definitionText}'")
        }
        val tokenizer = TantillaScanner(definitionText.code, if (applyOffset) definitionText.startPos else null)
        try {
            if (applyOffset) {
                println("######### first token: ${tokenizer.current}")
            }
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
                tokenizer.requireEof { "Trait methods must not have function bodies." }
                resolvedBody = TraitMethodBody(parentScope.nextTraitIndex++)
            } else {
                tokenizer.consume(":")
                docString = Parser.readDocString(tokenizer)
                resolvedBody =
                    Parser.parseDefinitionsAndStatements(tokenizer, 1, this, definitionScope = this, errorCollector = errorCollector)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw tokenizer.ensureParsingException(e)
        }
    }

    override fun eval(context: LocalRuntimeContext): Any {
        resolve()
        try {
            return resolvedBody!!.eval(context)
        } catch (e: ReturnSignal) {
            return e.value
        }
    }

    // Avoid calling serialization here, as this might lead to recursive crashes in error
    // reporting.
    override fun toString() = "def $name"

    override fun isSummaryExpandable() = true

    override fun serializeSummary(writer: CodeWriter, length: Definition.SummaryKind) {
        if (length == Definition.SummaryKind.EXPANDED) {
            serializeCode(writer)
        } else if (resolvedType == UnresolvedType) {
            writer.appendUnparsed(definitionText.code.split('\n').first(), userRootScope().definitionsWithErrors[this] ?: emptyList())
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
            writer.appendUnparsed(definitionText.code, userRootScope().definitionsWithErrors[this] ?: emptyList())
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

    override fun invalidate() {
        super.invalidate()
        resolvedType = UnresolvedType
        resolvedBody = UnresolvedNode
        locals.clear()
    }
}