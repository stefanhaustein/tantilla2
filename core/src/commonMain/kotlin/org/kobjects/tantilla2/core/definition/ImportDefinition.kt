package org.kobjects.tantilla2.core.definition

import org.kobjects.parserlib.tokenizer.ParsingException
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.type.NoneType
import org.kobjects.tantilla2.core.type.Type

class ImportDefinition(
    override val parentScope: Scope,
    val path: List<String>
) : Definition {
    var resolved: Definition? = null

    override val name: String
        get() = path.last()

    override val kind: Definition.Kind
        get() = Definition.Kind.IMPORT

    override fun getValue(self: Any?): Definition {
        resolve()
        return resolved!!
    }

    override fun resolve(applyOffset: Boolean, errorCollector: MutableList<ParsingException>?) {
        if (resolved == null) {
            var result = parentScope.resolveStatic(path[0], true)
            for (i in 1 until path.size) {
                result = (result as Scope).resolveStatic(path[i], false)
            }
            resolved = result
        }
    }

    override fun isSummaryExpandable() = false
    
    override fun serializeSummary(writer: CodeWriter, kind: Definition.SummaryKind) = serializeCode(writer)

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.appendKeyword("import ").append(path.joinToString("."))
    }

    override val type: Type
        get() {
            resolve()
            return resolved!!.type
        }
}