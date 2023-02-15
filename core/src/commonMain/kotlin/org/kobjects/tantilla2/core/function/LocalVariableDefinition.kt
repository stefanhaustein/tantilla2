package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.AbstractFieldDefinition
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.type.Type

class LocalVariableDefinition (
    override val parentScope: Scope,
    override val name: String,
    override val mutable: Boolean = false,
    override val type: Type,
    override var docString: String = "",
) : AbstractFieldDefinition() {

    override var index: Int = -1

    override val kind
        get() = Definition.Kind.PROPERTY


    override fun toString() = serializeCode()

    override fun isSummaryExpandable() = false

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
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

    override fun serializeSummary(writer: CodeWriter, length: Definition.SummaryKind) {
        serializeCode(writer)
    }



}