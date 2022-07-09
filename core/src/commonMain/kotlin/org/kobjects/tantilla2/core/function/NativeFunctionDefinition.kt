package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.*

class NativeFunctionDefinition(
    override val parentScope: Scope,
    override val kind: Definition.Kind,
    override val name: String,
    override var docString: String,
    override val type: FunctionType,
    val body: (RuntimeContext) -> Any?,
) : Callable, Definition {

    /*override val name: String
        get() = "(${type.parameters}) -> ${type.returnType}"
*/
    override fun eval(context: RuntimeContext) = body(context)
    override val value
        get() = this

    override fun serializeSummary(writer: CodeWriter) {
        serializeTitle(writer)
        writer.append('\n')
        writer.append(docString)
    }

    override fun serializeTitle(writer: CodeWriter) {
        writer.keyword("def ")
        writer.declaration(name)
        type.serializeType(writer)
    }

    override fun serializeCode(writer: CodeWriter, precedence: Int) = throw UnsupportedOperationException()

    override fun toString() = "def $name$type"
}