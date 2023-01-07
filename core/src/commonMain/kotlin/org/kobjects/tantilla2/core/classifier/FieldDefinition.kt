package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.definition.DefinitionUpdatable
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.node.expression.UnresolvedNode
import org.kobjects.tantilla2.core.parser.*
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.UnresolvedType

class FieldDefinition(
    override val parentScope: Scope,
    override val kind: Definition.Kind,
    override val name: String,
    definitionText: String = "",
    override val mutable: Boolean = false,
    override var docString: String = "",
) : Definition, DefinitionUpdatable {
    private var resolvedType: Type = UnresolvedType
    override var index: Int = -1

    private var _definitionText = definitionText

    override var definitionText
        get() = _definitionText
        set(value) {
            reset()
            _definitionText = value
        }

    private var resolvedInitializer: Node? = UnresolvedNode

    init {
        when (kind) {
            Definition.Kind.PROPERTY -> {
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


    override val type: Type
        get() {
            resolve(typeOnly = true)
            return resolvedType
        }

    override fun getValue(self: Any?): Any {
        resolve()
        return (self as LocalRuntimeContext)[index]
    }


    override fun setValue(self: Any?, newValue: Any) {
        resolve()
        (self as LocalRuntimeContext).variables[index] = newValue
    }

    fun initializer(): Node? {
        resolve()
        return resolvedInitializer
    }

    override fun resolve() {
        resolve(false)
    }

    private fun resolve(typeOnly: Boolean) {
        if (typeOnly) {
            if (resolvedType != UnresolvedType) {
                return
            }
        } else if (resolvedInitializer != UnresolvedNode) {
            return
        }

        val tokenizer = TantillaTokenizer(definitionText)
        tokenizer.consume(TokenType.BOF)

        tokenizer.tryConsume("static")
        tokenizer.tryConsume("def")
        tokenizer.tryConsume("mut")

        tokenizer.consume(name)

        val resolved = Parser.resolveVariable(tokenizer, ParsingContext(parentScope, 0), typeOnly)
        resolvedType = resolved.first
        if (typeOnly) {
            return
        }

        resolvedInitializer = resolved.third
        if (kind == Definition.Kind.STATIC) {
            index = parentScope.registerStatic(this)
        }
    }

    override fun toString() = serializeCode()

    // Used internally, only called when resolved
    private fun serializeDeclaration(writer: CodeWriter) {
        if (kind == Definition.Kind.STATIC && parentScope.supportsLocalVariables) {
            writer.appendKeyword("static ")
        }
        if (mutable) {
            writer.appendKeyword("mut ")
        }
        writer.appendDeclaration(name)
        writer.append(": ")
        writer.appendType(type)
    }

    override fun isSummaryExpandable(): Boolean {
        return resolvedInitializer != null
    }

    override fun serializeSummary(writer: CodeWriter, kind: Definition.SummaryKind) {
        if (kind == Definition.SummaryKind.EXPANDED) {
            serializeCode(writer)
        } else if (resolvedInitializer != UnresolvedNode) {
            serializeDeclaration(writer)
        } else {
            writer.appendUnparsed(definitionText.split("\n").first())
        }
    }


    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        if (resolvedInitializer != UnresolvedNode) {
            serializeSummary(writer, Definition.SummaryKind.COLLAPSED)
            if (resolvedInitializer != null) {
                writer.append(" = ")
                writer.appendCode(resolvedInitializer)
            }
        } else {
            writer.appendUnparsed(definitionText, userRootScope().definitionsWithErrors[this] ?: emptyList())
        }
    }

    override fun isDynamic() = kind == Definition.Kind.PROPERTY

    override fun findNode(node: Node): Definition? {
        val rid = resolvedInitializer
        if (rid != null && rid.containsNode(node)) {
            return this
        }
        return null
    }

    override fun reset() {
        resolvedInitializer = UnresolvedNode
        resolvedType = UnresolvedType
        super.reset()
    }

    fun initialize(staticVariableContext: LocalRuntimeContext) {
        resolve()
        if (kind == Definition.Kind.STATIC) {
           staticVariableContext.variables[index] = resolvedInitializer!!.eval(staticVariableContext)
        }
    }
}

