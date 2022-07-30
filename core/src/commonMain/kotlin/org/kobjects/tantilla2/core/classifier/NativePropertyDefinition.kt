package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*

class NativePropertyDefinition(
    override val parentScope: Scope,
    override val kind: Definition.Kind,
    override val name: String,
    override val type: Type,
    private val getter: (self: Any?) -> Any?,
    private val setter: ((self: Any?, newValue: Any?) -> Unit)? = null,
    override var docString: String = "",
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


    companion object {
        fun constant(
            parentScope: Scope,
            name: String,
            value: Any,
            docString: String = "",
        ) = NativePropertyDefinition(
            parentScope,
            Definition.Kind.STATIC,
            name,
            type = value.dynamicType,
            getter = { value },
            docString = docString
        )
    }
}