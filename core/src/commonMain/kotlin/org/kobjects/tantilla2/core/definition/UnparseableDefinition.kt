package org.kobjects.tantilla2.core.definition

import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.scope.Scope
import org.kobjects.tantilla2.core.type.Type

class UnparseableDefinition(
    override val parentScope: Scope?,
    override val name: String = "<Unparseable ${parentScope?.count() ?: 1}>",
    override var definitionText: CodeFragment
) : Definition, DefinitionUpdatable {

    override val kind: Definition.Kind
        get() = Definition.Kind.UNPARSEABLE

    override fun getValue(self: Any?) = this

    override val type: Type
        get() = throw UnsupportedOperationException()


    override fun resolve(applyOffset: Boolean, errorCollector: MutableList<ParsingException>?) {
        throw UnsupportedOperationException("Unparseable Definition.")
    }

    override fun isSummaryExpandable() = definitionText.code.contains("\n")

    override fun serializeSummary(writer: CodeWriter, kind: Definition.SummaryKind) {
        if (kind == Definition.SummaryKind.EXPANDED) {
            serializeCode(writer)
        } else {
            val cut = definitionText.code.indexOf('\n')
            writer.appendUnparsed(if (cut == -1) definitionText.code else definitionText.code.substring(0, cut))
        }
    }

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.appendUnparsed(definitionText.code)
    }

}