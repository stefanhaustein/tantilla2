package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.dynamicType

class NativePropertyDefinition(
    override val parentScope: Scope,
    override val kind: Definition.Kind,
    override val name: String,
    override var docString: String = "",
    override val type: Type,
    private val getter: (self: Any?) -> Any,
    private val setter: ((self: Any?, newValue: Any) -> Unit)? = null,
) : Definition {


    override fun getValue(self: Any?) = getter(self)

    override fun setValue(self: Any?, newValue: Any) {
        val setter = setter ?: throw UnsupportedOperationException()
        setter(self, newValue)
    }

    override val mutable: Boolean
        get() = setter != null


    override fun toString() = serializeCode()

    private fun serializeTitle(writer: CodeWriter) {
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

    override fun isSummaryExpandable() = docString.isNotEmpty() && docString.contains("\n")

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        serializeTitle(writer)
        writer.newline()
    }

    override fun serializeSummary(writer: CodeWriter, kind: Definition.SummaryKind) {
        serializeTitle(writer)
        if (docString.isNotEmpty() && kind != Definition.SummaryKind.NESTED) {
            writer.newline()
            writer.appendWrapped(CodeWriter.Kind.STRING,
                if (kind == Definition.SummaryKind.EXPANDED) docString else docString.split("\n").first())
        }
    }

    override fun isDynamic() = kind == Definition.Kind.PROPERTY

    companion object {
        fun constant(
            parentScope: Scope,
            name: String,
            value: Any,
            type: Type = value.dynamicType,
            docString: String = "",
        ) = NativePropertyDefinition(
            parentScope,
            Definition.Kind.STATIC,
            name,
            docString = docString,
            type = type,
            getter = { value }
        )
    }
}