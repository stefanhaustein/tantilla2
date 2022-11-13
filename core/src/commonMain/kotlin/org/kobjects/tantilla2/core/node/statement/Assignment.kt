package org.kobjects.tantilla2.core.node.statement

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.VoidType
import org.kobjects.tantilla2.core.node.Assignable
import org.kobjects.tantilla2.core.node.Evaluable
import org.kobjects.tantilla2.core.node.Node

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