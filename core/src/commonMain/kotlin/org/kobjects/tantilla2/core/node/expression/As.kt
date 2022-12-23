package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.Precedence
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.classifier.Adapter
import org.kobjects.tantilla2.core.classifier.ImplDefinition
import org.kobjects.tantilla2.core.node.Node

class As(
    val base: Node,
    val impl: ImplDefinition,
    val implicit: Boolean,
) : Node() {
    override val returnType: Type
        get() = impl.trait

    override fun children() = listOf(base, StaticReference(impl.trait, false))

    override fun eval(ctx: LocalRuntimeContext) =
        Adapter(impl.vmt, base.eval(ctx))

    override fun reconstruct(newChildren: List<Node>) =
        As(newChildren[0], impl, implicit)

    override fun serializeCode(sb: CodeWriter, parentPrecedence: Int) {
        if (implicit) {
            sb.appendCode(base)
        } else {
            sb.appendInfix(this, parentPrecedence, "as", Precedence.RELATIONAL)
        }
    }
}