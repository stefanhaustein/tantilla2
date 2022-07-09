package org.kobjects.tantilla2

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.*

class Unparseable(
    override val parentScope: Scope?,
    override val name: String = "(unparseable)",
    val definitionText: String
) : Definition {

    override val kind: Definition.Kind
        get() = Definition.Kind.UNPARSEABLE

    override val value: Any?
        get() = null

    override val type: Type
        get() = throw UnsupportedOperationException()

    override fun rebuild(compilationResults: CompilationResults) = false

    override fun serializeSummary(writer: CodeWriter) {
        serializeCode(writer)
    }

    override fun serializeTitle(writer: CodeWriter) {
        writer.append("(error: $name)")
    }

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.append(definitionText)
    }
}