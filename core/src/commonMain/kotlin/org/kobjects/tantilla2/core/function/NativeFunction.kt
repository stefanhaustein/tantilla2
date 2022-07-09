package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.*

class NativeFunction(
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
    override fun value() = this

    override fun valueType() = type
    override fun rebuild(compilationResults: CompilationResults): Boolean {
        TODO("Not yet implemented")
    }

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

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        TODO("Not yet implemented")
    }

    override fun toString() = "def $name$type"
}