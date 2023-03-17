package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.collection.ListType
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.commonType

class ListLiteral(
    val elements: List<Node>,
) : Node() {

    override fun eval(context: LocalRuntimeContext): List<Any> {
        return (returnType.unparameterized() as ListType).create(elements.size) { elements[it].eval(context)!! }
    }

    override val returnType = ListType().withGenericsResolved  (listOf(commonType(elements.map { it.returnType })))

    override fun children(): List<Node> = elements

    override fun reconstruct(newChildren: List<Node>) = ListLiteral(newChildren)

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.appendInBrackets("[", "]") {
            writer.appendList(elements)
        }
    }
}