package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.builtin.VoidType

class Assignment(
    val target: Assignable,
    val source: Evaluable
) : Node() {

    override val returnType: Type
        get() = VoidType

    override fun children() = listOf(target, source)

    override fun eval(ctx: LocalRuntimeContext) = target.assign(ctx, source.eval(ctx))

    override fun reconstruct(newChildren: List<Evaluable>) =
        Assignment(newChildren[0] as Assignable, newChildren[1])

    override fun serializeCode(sb: CodeWriter, parentPrecedence: Int) {
        sb.appendCode(target)
        sb.append(" = ")
        sb.appendCode(source)
    }
}