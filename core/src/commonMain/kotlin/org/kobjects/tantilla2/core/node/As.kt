package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.classifier.Adapter
import org.kobjects.tantilla2.core.classifier.ImplDefinition

class As(
    val base: Evaluable,
    val impl: ImplDefinition,
    val implicit: Boolean,
) : TantillaNode {
    override val returnType: Type
        get() = impl.trait

    override fun children() = listOf(base)

    override fun eval(ctx: LocalRuntimeContext) =
        Adapter(impl.vmt, base.eval(ctx))

    override fun reconstruct(newChildren: List<Evaluable>) =
        As(newChildren[0], impl, implicit)

    override fun serializeCode(sb: CodeWriter, precedence: Int) {
        sb.appendCode(base)
        if (!implicit) {
            sb.append(" as ")
            sb.append(impl.trait.name)
        }
    }
}