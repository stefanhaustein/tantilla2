package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.Type

class RawNode(val wrapped: Node) : Node() {
    override fun children() = listOf(wrapped)

    override fun reconstruct(newChildren: List<Node>) = RawNode(wrapped)

    override fun eval(context: LocalRuntimeContext) = wrapped.eval(context)

    override fun assign(context: LocalRuntimeContext, value: Any) = wrapped.assign(context, value)

    override fun requireAssignability() = wrapped.requireAssignability()

    override val returnType: Type
        get() = wrapped.returnType

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.appendCode(wrapped)
        writer.append('@')
    }
}