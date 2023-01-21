package org.kobjects.tantilla2.core.node.statement

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.NoneType
import org.kobjects.tantilla2.core.node.Node

class Assignment(
    target: Node,
    val source: Node
) : Node() {
    val target = target.requireAssignability()

    override val returnType: Type
        get() = NoneType

    override fun children() = listOf(target, source)

    override fun eval(ctx: LocalRuntimeContext) = target.assign(ctx, source.eval(ctx))

    override fun reconstruct(newChildren: List<Node>) =
        Assignment(newChildren[0], newChildren[1])

    override fun serializeCode(sb: CodeWriter, parentPrecedence: Int) {
        sb.appendCode(target)
        sb.append(" =")
        sb.appendMaybeNextLine(source)
    }

    init {
        require(source.returnType.isAssignableFrom(target.returnType)) {
            "Can't assign source type ${source.returnType} to an expression of type ${target.returnType}."
        }
    }
}