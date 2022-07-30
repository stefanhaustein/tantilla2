package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*

class NativePropertyDefinition (
    override val parentScope: Scope,
    override val kind: Definition.Kind,
    override val name: String,
    override val mutable: Boolean = false,
    private var resolvedValue: Any?,
    private var resolvedType: Type = resolvedValue.dynamicType,
    override var docString: String = "",
) : Definition {

    override var index: Int
        get() = -1
        set(value) = throw UnsupportedOperationException()

    override val type: Type
        get() = resolvedType

    override fun getValue(self: Any?) = resolvedValue

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
         writer.append(" # Native ")
    }

    override fun serializeSummary(writer: CodeWriter) {
        serializeCode(writer)
    }

    override fun isDynamic() = kind == Definition.Kind.PROPERTY

    override fun isScope() = false

}