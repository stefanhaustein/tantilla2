package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.*

class LocalVariableDefinition (
    override val parentScope: Scope,
    override val name: String,
    override val mutable: Boolean = false,
    override val type: Type,
    override var docString: String = "",
    override var index: Int = -1,
) : Definition {

    override val kind = Definition.Kind.PROPERTY

    init {

                val existingIndex = parentScope.definitions.locals.indexOf(name)
                if (index != existingIndex) {
                    throw IllegalArgumentException("local variable inconsistency new index: $index; existing: $existingIndex")
                }

    }

    override fun getValue(self: Any?) = (self as RuntimeContext)[index]

    override fun setValue(self: Any?, newValue: Any?) {
        (self as RuntimeContext).variables[index] = newValue
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


                   serializeTitle(writer)

    }

    override fun serializeSummary(writer: CodeWriter) {
        serializeCode(writer)
    }


    override fun isDynamic() = kind == Definition.Kind.PROPERTY

    override fun isScope() = false


}