package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.type.ListType
import org.kobjects.tantilla2.core.type.TypedList
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.commonType

class ListLiteral(
    val elements: List<Node>,
) : Node() {

    override fun eval(context: LocalRuntimeContext): TypedList {
        return returnType.create(elements.size) { elements[it].eval(context) }
    }

    override val returnType = ListType(commonType(elements.map { it.returnType }))

    override fun children(): List<Node> = elements

    override fun reconstruct(newChildren: List<Node>) = ListLiteral(newChildren)

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.append('[')
        if (elements.isNotEmpty()) {
            writer.appendCode(elements[0])
            for (i in 1 until elements.size) {
                writer.append(", ")
                writer.appendCode(elements[i])
            }
        }
        writer.append(']')
    }
}