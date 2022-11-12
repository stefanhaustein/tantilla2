package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.builtin.IntType
import org.kobjects.tantilla2.core.builtin.ListType
import org.kobjects.tantilla2.core.builtin.TypedList

class ElementAt(
    val baseExpr: Evaluable,
    val indexExpr: Evaluable
) : Assignable() {

    init {
        if (baseExpr.returnType !is ListType) {
            throw IllegalArgumentException("Base expression must be of list type")
        }
        if (indexExpr.returnType != IntType) {
            throw IllegalArgumentException("Index expression must be int.")
        }
    }

    override fun assign(context: LocalRuntimeContext, value: Any?) {
        val list = baseExpr.eval(context) as TypedList
        val index = indexExpr.evalF64(context).toInt()
        list[index] = value
    }


    override val returnType: Type
        get() = (baseExpr.returnType as ListType).elementType

    override fun children(): List<Evaluable> = listOf(baseExpr, indexExpr)

    override fun eval(context: LocalRuntimeContext): Any? {
        val list = baseExpr.eval(context) as TypedList
        val index = indexExpr.evalF64(context).toInt()
        if (index < 0 || index >= list.size) {
            throw context.globalRuntimeContext.createException(null, this, "List index $index out of range(0, ${list.size})")
        }
       return list[index]
    }

    override fun reconstruct(newChildren: List<Evaluable>): Evaluable =
        ElementAt(newChildren[0], newChildren[1])

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.appendCode(baseExpr)
        writer.append("[")
        writer.appendCode(indexExpr)
        writer.append("]")
    }
}