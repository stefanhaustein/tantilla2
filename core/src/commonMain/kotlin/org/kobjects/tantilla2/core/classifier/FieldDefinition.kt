package org.kobjects.tantilla2.core.classifier

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.builtin.RootScope
import org.kobjects.tantilla2.core.node.containsNode
import org.kobjects.tantilla2.core.parser.*

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


    private var currentValue: Any? = null

    private var resolvedInitializer: Evaluable<LocalRuntimeContext>? = null

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
                listOf(e)
            }
            return emptyList()
        }


    override val type: Type
        get() {
            resolve(typeOnly = true)
            return resolvedType!!
        }

    override fun getValue(self: Any?): Any? {
        resolve()
        return if (index == -1) currentValue else (self as LocalRuntimeContext)[index]
    }


    override fun setValue(self: Any?, newValue: Any?) {
        resolve()
        if (index == -1) {
            currentValue = newValue
        } else {
            (self as LocalRuntimeContext).variables[index] = newValue
        }
    }

    fun initializer(): Evaluable<LocalRuntimeContext>? {
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
                    parentScope.registerStatic(this)
                }
                resolutionState = ResolutionState.RESOLVED
            }


        } catch (e: Exception) {
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


    override fun serializeTitle(writer: CodeWriter, abbreviated: Boolean) {
        if (kind == Definition.Kind.STATIC && parentScope.supportsLocalVariables) {
            writer.appendKeyword("static ")
        }
        if (mutable) {
            writer.appendKeyword("mut ")
        }
        writer.appendDeclaration(name)
        writer.append(": ")
        writer.appendType(type, RootScope)
    }


    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        if (resolutionState == ResolutionState.RESOLVED) {
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


    override fun isDynamic() = kind == Definition.Kind.PROPERTY

    override fun isScope() = false

    override fun findNode(node: Evaluable<LocalRuntimeContext>): Definition? {
        val rid = resolvedInitializer
        if (rid != null && rid.containsNode(node)) {
            return this
        }
        return null
    }

    override fun reset() {
        resolutionState = ResolutionState.UNRESOLVED
        resolvedInitializer = null
        currentValue = null
        resolvedType = null

        super.reset()
    }

    fun initialize(globalRuntimeContext: GlobalRuntimeContext) {
        resolve()
        if (kind == Definition.Kind.STATIC) {
            currentValue = resolvedInitializer!!.eval(LocalRuntimeContext(globalRuntimeContext))
        }
    }


    enum class ResolutionState {
        UNRESOLVED, TYPE_RESOLVED, RESOLVED, ERROR
    }
}

