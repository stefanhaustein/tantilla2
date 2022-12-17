package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.collection.*
import org.kobjects.tantilla2.core.node.AssignableNode
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.*

class ElementAt(
    val baseExpr: Node,
    val keyExpr: Node
) : AssignableNode() {

    init {
        val baseType = baseExpr.returnType
        if (baseType is ListType
            || baseType is StrType) {
            if (keyExpr.returnType != IntType) {
                throw IllegalArgumentException("Index expression must be of type int.")
            }
        } else if (baseType is MapType) {
            if (!baseType.keyType.isAssignableFrom(keyExpr.returnType)) {
                throw IllegalArgumentException("Key expression must be of type ${baseType.keyType}")
            }
        } else {
            throw IllegalArgumentException("Base expression must be List, Map or str type for index access; actual type: $baseType")
        }
    }

    override fun assign(context: LocalRuntimeContext, value: Any) {
        val target = baseExpr.eval(context)
        if (target is MutableTypedList) {
            val index = keyExpr.evalF64(context).toInt()
            target[index] = value
        } else if (target is MutableTypedMap) {
            val key = keyExpr.eval(context)
            (target as MutableTypedMap).data[key] = value
        } else {
            throw RuntimeException("Can't assign to type ${baseExpr.returnType}")
        }
    }

    override val returnType: Type
        get() = when(baseExpr.returnType) {
            StrType -> StrType
            is ListType -> ((baseExpr.returnType) as ListType).elementType
            is MapType -> ((baseExpr.returnType) as MapType).valueType
            else -> throw IllegalArgumentException()
        }

    override fun children(): List<Node> = listOf(baseExpr, keyExpr)

    override fun eval(context: LocalRuntimeContext): Any {
        val base = baseExpr.eval(context)
        if (base is TypedMap) {
            val key = keyExpr.eval(context)
            return base[key!!]
        } else {
            var index = keyExpr.evalI64(context).toInt()
            if (base is String) {
                if (index < 0) {
                    index = base.length + index
                }
                return base.substring(index, index + 1)
            } else {
                val list = base as TypedList
                if (index < 0) {
                    index = list.size + index
                }
                if (index < 0 || index >= list.size) {
                    throw context.globalRuntimeContext.createException(null, this, "List index $index out of range(0, ${list.size})")
                }
                return list[index]
            }
        }
    }

    override fun reconstruct(newChildren: List<Node>) =
        ElementAt(newChildren[0], newChildren[1])

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.appendCode(baseExpr)
        writer.append("[")
        writer.appendCode(keyExpr)
        writer.append("]")
    }
}