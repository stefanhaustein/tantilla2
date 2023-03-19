package org.kobjects.tantilla2.core.scope

import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.CodeFragment
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.DefinitionUpdatable
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.node.expression.StaticReference
import org.kobjects.tantilla2.core.node.expression.UnresolvedNode
import org.kobjects.tantilla2.core.parser.*
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.UnresolvedType

class FieldDefinition(
    override val parentScope: Scope,
    override val kind: Definition.Kind,
    override val name: String,
    definitionText: CodeFragment,
    override val mutable: Boolean = false,
    override var docString: String = "",
) : AbstractFieldDefinition() , DefinitionUpdatable {
    private var resolvedType: Type = UnresolvedType
    override var index: Int = -1

    private var _definitionText = definitionText

    override var definitionText
        get() = _definitionText
        set(value) {
            invalidate()
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

    override fun compareTo(other: Definition): Int {
        if (other is FieldDefinition && other.kind == kind && kind == Definition.Kind.PROPERTY) {
            val d = index.compareTo(other.index)
            if (d != 0) {
                return d
            }
        }
        return super.compareTo(other)
    }

    override val type: Type
        get() {
            resolve(typeOnly = true, null, false)
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

    override fun resolve(applyOffset: Boolean, errorCollector: MutableList<ParsingException>?) {
        resolve(false, errorCollector, applyOffset)
    }

    private fun resolve(typeOnly: Boolean, errorCollector: MutableList<ParsingException>?, applyOffset: Boolean) {
        if (typeOnly) {
            if (resolvedType != UnresolvedType) {
                return
            }
        } else if (resolvedInitializer != UnresolvedNode) {
            return
        }

        val tokenizer = TantillaScanner(definitionText.code)

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
            // Make sure dependencies are resolved first and get a lower index
            resolvedInitializer?.recurse {
                if (it is StaticReference) {
                    it.definition.resolve(errorCollector = errorCollector)
                }
            }

            index = parentScope.registerStatic(this)
        }
    }


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
            writer.appendUnparsed(definitionText.code.split("\n").first())
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
            writer.appendUnparsed(definitionText.code, userRootScope().definitionsWithErrors[this] ?: emptyList())
        }
    }

    override fun findNode(node: Node): Definition? {
        val rid = resolvedInitializer
        if (rid != null && rid.containsNode(node)) {
            return this
        }
        return null
    }

    override fun invalidate() {
        resolvedInitializer = UnresolvedNode
        resolvedType = UnresolvedType
        super.invalidate()
    }

    fun initialize(staticVariableContext: LocalRuntimeContext) {
        try {
            resolve()
            if (kind == Definition.Kind.STATIC) {
                staticVariableContext.variables[index] =
                    resolvedInitializer!!.eval(staticVariableContext)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw staticVariableContext.globalRuntimeContext.ensureTantillaRuntimeException(e, this, resolvedInitializer)
        }
    }
}

