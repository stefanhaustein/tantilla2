package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.classifier.Adapter
import org.kobjects.tantilla2.core.classifier.ImplDefinition

class As(
    val base: Evaluable<RuntimeContext>,
    val impl: ImplDefinition,
    val implicit: Boolean,
) : TantillaNode {
    override val returnType: Type
        get() = impl.trait

    override fun children() = listOf(base)

    override fun eval(ctx: RuntimeContext) =
        Adapter(impl.vmt, base.eval(ctx) as RuntimeContext)

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) =
        As(newChildren[0], impl, implicit)

    override fun serializeCode(sb: CodeWriter, precedence: Int) {
        sb.appendCode(base)
        if (!implicit) {
            sb.append(" as ")
            sb.append(impl.trait.name)
        }
    }
}