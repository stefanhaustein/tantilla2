package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.node.AssignableNode
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.*

class ElementAt(
    val baseExpr: Node,
    val indexExpr: Node
) : AssignableNode() {

    init {
        if (baseExpr.returnType !is ListType && baseExpr.returnType !is MutableListType) {
            throw IllegalArgumentException("Base expression must be of list type")
        }
        if (indexExpr.returnType != IntType) {
            throw IllegalArgumentException("Index expression must be int.")
        }
    }

    override fun assign(context: LocalRuntimeContext, value: Any?) {
        val list = baseExpr.eval(context) as MutableTypedList
        val index = indexExpr.evalF64(context).toInt()
        list[index] = value
    }


    override val returnType: Type
        get() = (baseExpr.returnType as ListType).elementType

    override fun children(): List<Node> = listOf(baseExpr, indexExpr)

    override fun eval(context: LocalRuntimeContext): Any? {
        val list = baseExpr.eval(context) as TypedList
        val index = indexExpr.evalF64(context).toInt()
        if (index < 0 || index >= list.size) {
            throw context.globalRuntimeContext.createException(null, this, "List index $index out of range(0, ${list.size})")
        }
       return list[index]
    }

    override fun reconstruct(newChildren: List<Node>) =
        ElementAt(newChildren[0], newChildren[1])

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.appendCode(baseExpr)
        writer.append("[")
        writer.appendCode(indexExpr)
        writer.append("]")
    }
}