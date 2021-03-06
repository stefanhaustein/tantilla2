package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.runtime.ListType
import org.kobjects.tantilla2.core.runtime.TypedList

class ListLiteral(
    val elements: List<Evaluable<RuntimeContext>>,
) : TantillaNode {

    override fun eval(context: RuntimeContext): TypedList {
        return returnType.create(elements.size) { elements[it].eval(context) }
    }

    override val returnType = ListType(commonType(elements.map { it.returnType }))

    override fun children(): List<Evaluable<RuntimeContext>> = elements

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) = ListLiteral(newChildren)

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
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