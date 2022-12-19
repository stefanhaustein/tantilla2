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

    override fun isSummaryExpandable() = definitionText.contains("\n")

    override fun serializeSummary(writer: CodeWriter, kind: Definition.SummaryKind) {
        if (kind == Definition.SummaryKind.EXPANDED) {
            serializeCode(writer)
        } else {
            val cut = definitionText.indexOf('\n')
            writer.appendUnparsed(if (cut == -1) definitionText else definitionText.substring(0, cut))
        }
    }

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.appendUnparsed(definitionText)
    }

}