package org.kobjects.tantilla2.core.definition

import org.kobjects.tantilla2.core.CodeWriter

class EnumLiteral(
    override val parentScope: EnumDefinition,
    override val name: String
) : Definition {

    override val kind: Definition.Kind
        get() = Definition.Kind.ENUM_LITERAL

    override fun getValue(self: Any?): Any? {
        TODO("Not yet implemented")
    }

    override fun serializeSummary(writer: CodeWriter) {
        TODO("Not yet implemented")
    }

    override fun serializeTitle(writer: CodeWriter, abbreviated: Boolean) {
        TODO("Not yet implemented")
    }

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        TODO("Not yet implemented")
    }
}