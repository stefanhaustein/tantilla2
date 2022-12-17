package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.definition.Scope

class NativeFunctionDefinition(
    override val parentScope: Scope,
    override val kind: Definition.Kind,
    override val name: String,
    override var docString: String,
    override val type: FunctionType,
    val body: (LocalRuntimeContext) -> Any,
) : Callable, Definition {

    /*override val name: String
        get() = "(${type.parameters}) -> ${type.returnType}"
*/
    override fun eval(context: LocalRuntimeContext) = body(context)

    override fun getValue(self: Any?): NativeFunctionDefinition = this


    override fun serializeSummary(writer: CodeWriter) {
        serializeTitle(writer)
        writer.newline()
        if (docString.isEmpty()) {
            writer.newline()
        } else {
            writer.appendWrapped(CodeWriter.Kind.STRING, docString)
        }
    }

    override fun serializeTitle(writer: CodeWriter, abbreviated: Boolean) {
        if (parentScope.supportsMethods && kind == Definition.Kind.FUNCTION) {
            writer.appendKeyword("static ")
        }
        writer.appendKeyword("def ")
        writer.appendDeclaration(name)
        type.serializeType(writer, if (abbreviated) null else parentScope)
    }

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) = throw UnsupportedOperationException()

    override fun toString() = "def $name$type"
}