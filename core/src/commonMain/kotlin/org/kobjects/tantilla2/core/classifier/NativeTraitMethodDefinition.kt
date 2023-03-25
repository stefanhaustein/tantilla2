package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.scope.Scope
import org.kobjects.tantilla2.core.type.Type

open class NativeTraitMethodDefinition(
    override val parentScope: TraitDefinition,
    override val name: String,
    override var docString: String,
    override var type: FunctionType

) : Callable, Definition {

    val vmtIndex = parentScope.nextTraitIndex++

    override val kind: Definition.Kind
        get() = Definition.Kind.METHOD

    /*override val name: String
        get() = "(${type.parameters}) -> ${type.returnType}"
*/
    override fun eval(context: LocalRuntimeContext) =
        context.getAdapter().evalMethod(vmtIndex, context)

    override fun getValue(self: Any?): NativeTraitMethodDefinition = this

    override fun isSummaryExpandable(): Boolean =
        docString.isNotEmpty() || type.parameters.isNotEmpty()

    override fun serializeSummary(writer: CodeWriter, length: Definition.SummaryKind) {
        writer.appendKeyword("def ")
        writer.appendDeclaration(name)
        writer.appendType(type)
        if (length == Definition.SummaryKind.EXPANDED) {
            writer.newline()
            writer.appendWrapped(CodeWriter.Kind.STRING, docString)
        }
    }

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) =
        throw UnsupportedOperationException()

    override fun toString() = "def $name$type"

    override fun withTypesMapped(newParent: Scope, mapping: (Type) -> Type) =
        ParameterizedTraitMethodDefinition(newParent as TraitDefinition, this, mapping)
}