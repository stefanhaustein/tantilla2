package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.builtin.ListType
import org.kobjects.tantilla2.core.builtin.TypedList

class ListLiteral(
    val elements: List<Evaluable>,
) : Node() {

    override fun eval(context: LocalRuntimeContext): TypedList {
        return returnType.create(elements.size) { elements[it].eval(context) }
    }

    override val returnType = ListType(commonType(elements.map { it.returnType }))

    override fun children(): List<Evaluable> = elements

    override fun reconstruct(newChildren: List<Evaluable>) = ListLiteral(newChildren)

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