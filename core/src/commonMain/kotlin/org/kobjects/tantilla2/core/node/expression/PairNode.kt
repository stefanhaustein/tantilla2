package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.collection.PairType
import org.kobjects.tantilla2.core.node.Node

class PairNode(override val returnType: PairType, val a: Node, val b: Node) : Node() {
    override fun children() = listOf(a, b)

    override fun reconstruct(newChildren: List<Node>) = PairNode(returnType, newChildren[0], newChildren[1])

    override fun eval(context: LocalRuntimeContext) = Pair(a.eval(context), b.eval(context))

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.append("Pair(")
        writer.appendCode(a)
        writer.append(", ")
        writer.appendCode(b)
        writer.append(")")
    }
}