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

    override val mutable: Boolean
        get() = false

    override var docString: String
        get() = ""
        set(_) = throw UnsupportedOperationException()

    override var index: Int
        get() = -1
        set(_) = throw UnsupportedOperationException()

    override fun value(): Any? = null

    override fun valueType(): Type {
        TODO("Not yet implemented")
    }

    override fun error(): Exception? = null

    override fun depth(scope: Scope): Int = throw UnsupportedOperationException()

    override fun findNode(node: Evaluable<RuntimeContext>): Definition? = null

    override fun isDynamic() = false

    override fun isScope() = false

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