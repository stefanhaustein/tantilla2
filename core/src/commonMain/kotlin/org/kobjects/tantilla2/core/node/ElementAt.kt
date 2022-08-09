package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.runtime.F64
import org.kobjects.tantilla2.core.runtime.ListType
import org.kobjects.tantilla2.core.runtime.TypedList

class ElementAt(
    val baseExpr: Evaluable<LocalRuntimeContext>,
    val indexExpr: Evaluable<LocalRuntimeContext>) : Assignable {

    init {
        if (baseExpr.returnType !is ListType) {
            throw IllegalArgumentException("Base expression must be of list type")
        }
        if (indexExpr.returnType != F64) {
            throw IllegalArgumentException("Index expression must be number")
        }
    }

    override fun assign(context: LocalRuntimeContext, value: Any?) {
        val list = baseExpr.eval(context) as TypedList
        val index = indexExpr.evalF64(context).toInt()
        list[index] = value
    }


    override val returnType: Type
        get() = (baseExpr.returnType as ListType).elementType

    override fun children(): List<Evaluable<LocalRuntimeContext>> = listOf(baseExpr, indexExpr)

    override fun eval(context: LocalRuntimeContext): Any? {
        val list = baseExpr.eval(context) as TypedList
        val index = indexExpr.evalF64(context).toInt()
        if (index < 0 || index >= list.size) {
            throw TantillaRuntimeException(this, "List index $index out of range(0, ${list.size}")
        }
       return list[index]
    }

    override fun reconstruct(newChildren: List<Evaluable<LocalRuntimeContext>>): Evaluable<LocalRuntimeContext> =
        ElementAt(newChildren[0], newChildren[1])

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.appendCode(baseExpr)
        writer.append("[")
        writer.appendCode(indexExpr)
        writer.append("]")
    }
}