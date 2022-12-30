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

    override fun isSummaryExpandable(): Boolean = docString.isNotEmpty() || type.parameters.isNotEmpty()

    override fun serializeSummary(writer: CodeWriter, length: Definition.SummaryKind) {
        if (parentScope.supportsMethods && kind == Definition.Kind.FUNCTION) {
            writer.appendKeyword("static ")
        }
        writer.appendKeyword("def ")
        writer.appendDeclaration(name)
        writer.appendType(type)
        if (length == Definition.SummaryKind.EXPANDED) {
            writer.newline()
            writer.appendWrapped(CodeWriter.Kind.STRING, docString)
        }
    }

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) = throw UnsupportedOperationException()

    override fun toString() = "def $name$type"
}