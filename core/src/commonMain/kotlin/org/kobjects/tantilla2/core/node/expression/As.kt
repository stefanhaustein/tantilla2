package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
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

    override fun children() = listOf(base)

    override fun eval(ctx: LocalRuntimeContext) =
        Adapter(impl.vmt, base.eval(ctx))

    override fun reconstruct(newChildren: List<Node>) =
        As(newChildren[0], impl, implicit)

    override fun serializeCode(sb: CodeWriter, parentPrecedence: Int) {
        sb.appendCode(base)
        if (!implicit) {
            sb.append(" as ")
            sb.append(impl.trait.name)
        }
    }
}