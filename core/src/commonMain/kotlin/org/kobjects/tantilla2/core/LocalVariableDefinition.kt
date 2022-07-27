package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.node.TantillaNode
import org.kobjects.tantilla2.core.node.containsNode
import org.kobjects.tantilla2.core.parser.*

class LocalVariableDefinition (
    override val parentScope: Scope,
    override val kind: Definition.Kind,
    override val name: String,
    override val mutable: Boolean = false,
    override val type: Type,
    override var docString: String = "",
    override var index: Int = -1,
) : Definition {

    init {
        when (kind) {
            Definition.Kind.FIELD -> {
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



    override var value: Any?
        get() = throw UnsupportedOperationException()
        set(value) = throw UnsupportedOperationException()



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


                   serializeTitle(writer)

    }

    override fun serializeSummary(writer: CodeWriter) {
        serializeCode(writer)
    }


    override fun isDynamic() = kind == Definition.Kind.FIELD

    override fun isScope() = false


}