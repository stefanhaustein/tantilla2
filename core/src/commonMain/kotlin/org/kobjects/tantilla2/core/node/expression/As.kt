package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.Precedence
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.classifier.AdapterInstance
import org.kobjects.tantilla2.core.classifier.ImplDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.node.Node

class As(
    val base: Node,
    val impl: ImplDefinition,
    val trait: TraitDefinition,
    val implicit: Boolean = false,
) : Node() {
    override val returnType: Type
        get() = trait

    override fun children() = listOf(base)

    override fun eval(ctx: LocalRuntimeContext) =
        AdapterInstance(impl.vmt, base.eval(ctx))

    override fun reconstruct(newChildren: List<Node>) =
        As(newChildren[0], impl, trait, implicit)

    override fun serializeCode(sb: CodeWriter, parentPrecedence: Int) {
        if (implicit) {
            sb.appendCode(base)
        } else {
            sb.appendInfix(parentPrecedence, base, "as", Precedence.RELATIONAL, StaticReference(trait,  true))
        }
    }
}