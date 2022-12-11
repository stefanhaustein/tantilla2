package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.collection.ListType
import org.kobjects.tantilla2.core.collection.TypedList
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.collection.MutableListType
import org.kobjects.tantilla2.core.type.commonType

class ListLiteral(
    val elements: List<Node>,
    val mutable: Boolean
) : Node() {

    override fun eval(context: LocalRuntimeContext): TypedList {
        return returnType.create(elements.size) { elements[it].eval(context)!! }
    }

    override val returnType = if (mutable) MutableListType(commonType(elements.map { it.returnType })) else ListType(commonType(elements.map { it.returnType }))

    override fun children(): List<Node> = elements

    override fun reconstruct(newChildren: List<Node>) = ListLiteral(newChildren, mutable)

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        if (mutable) {
            writer.append("mut ")
        }
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