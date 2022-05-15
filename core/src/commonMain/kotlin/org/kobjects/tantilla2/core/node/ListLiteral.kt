package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.runtime.ListType
import org.kobjects.tantilla2.core.runtime.TypedList

class ListLiteral(
    val elements: List<Evaluable<RuntimeContext>>,
) : Evaluable<RuntimeContext>, SerializableCode {

    override fun eval(context: RuntimeContext): TypedList {
        return type.create(elements.size) { elements[it].eval(context) }
    }

    override val type = ListType(commonType(elements.map { it.type }))

    override fun children(): List<Evaluable<RuntimeContext>> = elements

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) = ListLiteral(newChildren)

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.append('[')
        if (elements.isNotEmpty()) {
            elements[0].serializeCode(writer)
            for (i in 1 until elements.size) {
                writer.append(", ")
                elements[i].serializeCode(writer)
            }
        }
        writer.append(']')
    }
}