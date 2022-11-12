package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*

class NativePropertyDefinition(
    override val parentScope: Scope,
    override val kind: Definition.Kind,
    override val name: String,
    override var docString: String = "",
    override val type: Type,
    private val getter: (self: Any?) -> Any?,
    private val setter: ((self: Any?, newValue: Any?) -> Unit)? = null,
) : Definition {

    override var index: Int
        get() = -1
        set(value) = throw UnsupportedOperationException()

    override fun getValue(self: Any?) = getter(self)

    override fun setValue(self: Any?, newValue: Any?) {
        val setter = setter ?: throw UnsupportedOperationException()
        setter(self, newValue)
    }

    override val mutable: Boolean
        get() = setter != null


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
        writer.appendType(type, parentScope)
    }


    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
         serializeTitle(writer)
    }

    override fun serializeSummary(writer: CodeWriter) {
        serializeTitle(writer)
        writer.newline()
        if (docString.isNotEmpty()) {
            writer.appendWrapped(CodeWriter.Kind.STRING, docString)
        } else {
            writer.newline()
        }
    }

    override fun isDynamic() = kind == Definition.Kind.PROPERTY

    override fun isScope() = false


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