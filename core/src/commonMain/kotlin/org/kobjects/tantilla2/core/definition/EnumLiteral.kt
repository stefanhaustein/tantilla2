package org.kobjects.tantilla2.core.definition

import org.kobjects.tantilla2.core.CodeWriter

class EnumLiteral(
    override val parentScope: EnumDefinition,
    override val name: String
) : Definition {

    override val kind: Definition.Kind
        get() = Definition.Kind.ENUM_LITERAL

    override fun getValue(self: Any?) = this

    override fun serializeSummary(writer: CodeWriter) {
        writer.append(name)
    }

    override fun serializeTitle(writer: CodeWriter, abbreviated: Boolean) {
        writer.append(name)
    }

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.append(name)
        writer.newline()
    }
}