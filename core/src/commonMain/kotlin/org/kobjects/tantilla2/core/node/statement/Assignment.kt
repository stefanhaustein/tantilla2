package org.kobjects.tantilla2.core.node.statement

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.VoidType
import org.kobjects.tantilla2.core.node.Node

class Assignment(
    val target: Node,
    val source: Node
) : Node() {

    override val returnType: Type
        get() = VoidType

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
        require(target.isAssignable()) {
            "Target expression '$target' is not assignable (= immutable?)."
        }
        require(source.returnType.isAssignableFrom(target.returnType)) {
            "Can't assign source type ${source.returnType} to an expression of type ${target.returnType}."
        }
    }
}