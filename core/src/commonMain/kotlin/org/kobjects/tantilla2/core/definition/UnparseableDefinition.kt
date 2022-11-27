package org.kobjects.tantilla2.core.definition

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.CompilationResults
import org.kobjects.tantilla2.core.type.Type

class UnparseableDefinition(
    override val parentScope: Scope?,
    override val name: String = "<Unparseable ${parentScope?.count() ?: 1}>",
    val definitionText: String
) : Definition {

    override val kind: Definition.Kind
        get() = Definition.Kind.UNPARSEABLE

    override fun getValue(self: Any?) = throw UnsupportedOperationException()

    override val type: Type
        get() = throw UnsupportedOperationException()

    override fun resolveAll(compilationResults: CompilationResults) = false

    override fun serializeSummary(writer: CodeWriter) {
       serializeCode(writer)
    }

    override fun serializeTitle(writer: CodeWriter, abbreviated: Boolean) {
       val cut = definitionText.indexOf('\n')
        writer.appendUnparsed(if (!abbreviated || cut == -1) definitionText else definitionText.substring(0, cut))
    }

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.appendUnparsed(definitionText)
    }

}