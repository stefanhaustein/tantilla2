package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.classifier.FieldDefinition

class UserRootScope(
    override val parentScope: Scope,
) : Scope() {
    val staticFields = mutableListOf<FieldDefinition>()

    override val kind: Definition.Kind
        get() = Definition.Kind.UNIT

    override val name: String
        get() = "<UserScope>"

    override var docString: String
        get() = ""
        set(_) = throw UnsupportedOperationException()

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        serializeBody(writer)
    }

    override fun registerStatic(fieldDefinition: FieldDefinition) {
        staticFields.add(fieldDefinition)
    }

    override fun reset() {
        super.reset()
        staticFields.clear()
    }
}