package org.kobjects.tantilla2.core.scope

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.type.Type

class EnumLiteral(
    override val parentScope: EnumDefinition,
    override val name: String
) : Definition {

    override val kind: Definition.Kind
        get() = Definition.Kind.ENUM_LITERAL

    override fun getValue(self: Any?) = this

    override fun isSummaryExpandable() = false

    override fun serializeSummary(writer: CodeWriter, kind: Definition.SummaryKind) {
        writer.append(name)
    }


    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.append(name)
        writer.newline()
    }

    override val type: Type
        get() = parentScope
}