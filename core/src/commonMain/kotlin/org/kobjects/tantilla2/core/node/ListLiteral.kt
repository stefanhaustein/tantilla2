package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.commonType
import org.kobjects.tantilla2.core.runtime.ListType
import org.kobjects.tantilla2.core.runtime.TypedList
import org.kobjects.tantilla2.core.serialize

class ListLiteral(
    val elements: List<Evaluable<RuntimeContext>>,
) : Evaluable<RuntimeContext>, Serializable {

    override fun eval(context: RuntimeContext): TypedList {
        return type.create(elements.size) { elements[it].eval(context) }
    }

    override val type = ListType(commonType(elements.map { it.type }))

    override fun children(): List<Evaluable<RuntimeContext>> = elements

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) = ListLiteral(newChildren)

    override fun serialize(writer: CodeWriter, prcedence: Int) {
        writer.append('[')
        if (elements.isNotEmpty()) {
            elements[0].serialize(writer)
            for (i in 1 until elements.size) {
                writer.append(", ")
                elements[i].serialize(writer)
            }
        }
        writer.append(']')
    }
}