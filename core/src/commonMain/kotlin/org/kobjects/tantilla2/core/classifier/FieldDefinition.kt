package org.kobjects.tantilla2.core.classifier

import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.parser.*
import org.kobjects.tantilla2.core.type.Type

class FieldDefinition(
    override val parentScope: Scope,
    override val kind: Definition.Kind,
    override val name: String,
    definitionText: String = "",
    override val mutable: Boolean = false,
    override var docString: String = "",
) : Definition, Updatable {
    private var resolvedType: Type? = null
    override var index: Int = -1
    private var resolutionState: ResolutionState = ResolutionState.UNRESOLVED
    var error: ParsingException? = null

    private var _definitionText = definitionText

    override var definitionText
        get() = _definitionText
        set(value) {
            resolutionState = ResolutionState.RESOLVED
            _definitionText = value
        }

    private var resolvedInitializer: Node? = null

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


    override val errors: List<Exception>
        get() {
            try {
                resolve()
            } catch (e: Exception) {
                e.printStackTrace()
                listOf(e)
            }
            return emptyList()
        }


    override val type: Type
        get() {
            resolve(typeOnly = true)
            return resolvedType!!
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
        resolvedInitializer = null

        try {
            tokenizer.tryConsume("static")
            tokenizer.tryConsume("mut")
            tokenizer.tryConsume("var") || tokenizer.tryConsume("val") // var/val

            tokenizer.consume(name)

            val resolved =
                Parser.resolveVariable(tokenizer, ParsingContext(parentScope, 0), typeOnly)
            resolvedType = resolved.first

            if (typeOnly) {
                resolutionState = ResolutionState.TYPE_RESOLVED
            } else {
                resolvedInitializer = resolved.third
                if (kind == Definition.Kind.STATIC) {
                    index = parentScope.registerStatic(this)
                }
                resolutionState = ResolutionState.RESOLVED
            }


        } catch (e: Exception) {
            e.printStackTrace()
            resolutionState = ResolutionState.ERROR
            if (e is ParsingException) {
                error = e
            } else {
                error = ParsingException(
                    tokenizer.current,
                    "Error in ${parentScope.name}.$name: " + (e.message ?: "Parsing Error"),
                    e
                )
            }
            throw error!!
        }
    }


    override fun toString() = serializeCode()

    private fun serializeDeclaration(writer: CodeWriter) {
        if (kind == Definition.Kind.STATIC && parentScope.supportsLocalVariables) {
            writer.appendKeyword("static ")
        }
        if (mutable) {
            writer.appendKeyword("mut ")
        }
        writer.appendDeclaration(name)
        writer.append(": ")
        writer.appendType(type, parentScope)
    }

    override fun isSummaryExpandable(): Boolean {
        return resolvedInitializer != null
    }

    override fun serializeSummary(writer: CodeWriter, kind: Definition.SummaryKind) {
        if (kind == Definition.SummaryKind.EXPANDED) {
            serializeCode(writer)
        } else if (resolutionState == ResolutionState.RESOLVED) {
            serializeDeclaration(writer)
        } else {
            writer.appendUnparsed(definitionText.split("\n").first())
        }
    }


    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        if (resolutionState == ResolutionState.RESOLVED) {
            serializeSummary(writer, Definition.SummaryKind.COLLAPSED)
            if (resolvedInitializer != null) {
                writer.append(" = ")
                writer.appendCode(resolvedInitializer)
            }
        } else {
            writer.appendUnparsed(definitionText, errors)
        }
    }


    override fun isDynamic() = kind == Definition.Kind.PROPERTY

    override fun isScope() = false

    override fun findNode(node: Node): Definition? {
        val rid = resolvedInitializer
        if (rid != null && rid.containsNode(node)) {
            return this
        }
        return null
    }

    override fun reset() {
        resolutionState = ResolutionState.UNRESOLVED
        resolvedInitializer = null
        resolvedType = null

        super.reset()
    }

    fun initialize(staticVariableContext: LocalRuntimeContext) {
        resolve()
        if (kind == Definition.Kind.STATIC) {
           staticVariableContext.variables[index] = resolvedInitializer!!.eval(staticVariableContext)
        }
    }


    enum class ResolutionState {
        UNRESOLVED, TYPE_RESOLVED, RESOLVED, ERROR
    }
}

