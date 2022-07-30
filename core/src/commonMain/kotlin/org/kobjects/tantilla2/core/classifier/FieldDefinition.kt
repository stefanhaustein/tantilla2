package org.kobjects.tantilla2.core.classifier

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.node.containsNode
import org.kobjects.tantilla2.core.parser.*

class FieldDefinition(
    override val parentScope: Scope,
    override val kind: Definition.Kind,
    override val name: String,
    val definitionText: String = "",
    override val mutable: Boolean = false,
    override var docString: String = "",
) : Definition {
    private var resolvedType: Type? = null
    private var resolvedValue: Any? = null
    override var index: Int = -1
    private var resolutionState: ResolutionState = ResolutionState.UNRESOLVED
    var error: ParsingException? = null

    private var resolvedInitializer: Evaluable<RuntimeContext>? = null

    init {
        when (kind) {
            Definition.Kind.PROPERTY -> {
                val existingIndex = parentScope.definitions.locals.indexOf(name)
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
        return if (index == -1) resolvedValue else (self as RuntimeContext)[index]
    }


    override fun setValue(self: Any?, newValue: Any?) {
        resolve()
        if (index == -1) {
            resolvedValue = newValue
        } else {
            (self as RuntimeContext).variables[index] = newValue
        }
    }

    fun initializer(): Evaluable<RuntimeContext>? {
        resolve()
        return resolvedInitializer
    }

    private fun resolve(typeOnly: Boolean = false) {
        when (resolutionState) {
            ResolutionState.RESOLVED -> return
            ResolutionState.TYPE_RESOLVED -> if (typeOnly) return
            ResolutionState.ERROR -> {
                throw error!!
            }
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
                    resolvedValue = resolvedInitializer!!.eval(RuntimeContext(mutableListOf()))
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


    override fun serializeTitle(writer: CodeWriter) {
        if (kind == Definition.Kind.STATIC && parentScope.supportsLocalVariables) {
            writer.keyword("static ")
        }
        if (mutable) {
            writer.keyword("mut ")
        }
        writer.declaration(name)
        writer.append(": ")
        writer.appendType(type)
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

    override fun findNode(node: Evaluable<RuntimeContext>): Definition? {
        val rid = resolvedInitializer
        if (rid != null && rid.containsNode(node)) {
            return this
        }
        return null
    }

    enum class ResolutionState {
        UNRESOLVED, TYPE_RESOLVED, RESOLVED, ERROR
    }
}

