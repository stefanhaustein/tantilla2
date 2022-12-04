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
        if (baseExpr.returnType !is ListType && baseExpr.returnType !is MutableListType && baseExpr.returnType != StrType) {
            throw IllegalArgumentException("Base expression must be of list or str type")
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
        get() = if (baseExpr.returnType == StrType) StrType else (baseExpr.returnType as ListType).elementType

    override fun children(): List<Node> = listOf(baseExpr, indexExpr)

    override fun eval(context: LocalRuntimeContext): Any? {
        val index = indexExpr.evalF64(context).toInt()
        if (baseExpr.returnType == StrType) {
            val s = baseExpr.eval(context) as String
            return s.substring(index, index + 1)
        } else {
        val list = baseExpr.eval(context) as TypedList
        if (index < 0 || index >= list.size) {
            throw context.globalRuntimeContext.createException(null, this, "List index $index out of range(0, ${list.size})")
        }
       return list[index]
        }
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