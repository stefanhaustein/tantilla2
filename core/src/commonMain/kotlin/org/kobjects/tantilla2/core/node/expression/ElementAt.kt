package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.*
import org.kobjects.tantilla2.core.collection.*
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.*

class ElementAt(
    val baseExpr: Node,
    val keyExpr: Node
) : Node() {

    init {
        val baseType = baseExpr.returnType
        val unparameterized = baseType.unparameterized()
        if (unparameterized is ListType
            || unparameterized is StrType) {
            if (keyExpr.returnType != IntType) {
                throw IllegalArgumentException("Index expression must be of type int; got: $baseType")
            }
        } else if (unparameterized is MapType) {
            val keyType = baseType.genericParameterTypes[0]
            if (!keyType.isAssignableFrom(keyExpr.returnType)) {
                throw IllegalArgumentException("Key expression must be of type ${keyType}")
            }
        } else {
            throw IllegalArgumentException("Base expression must be List, Map or str type for index access; actual type: $baseType")
        }
    }

    override fun requireAssignability(): Node {
        val baseType = baseExpr.returnType.unparameterized ?: baseExpr.returnType
        if (baseType !is MutableListType && baseType !is MutableMapType) {
            throw IllegalArgumentException("MutableMap or MutableList required for assignment but got type '$baseType' for expression '$baseExpr'")
        }
        return this
    }

    override fun assign(context: LocalRuntimeContext, value: Any) {
        val target = baseExpr.eval(context)
        if (target is MutableList<*>) {
            val index = keyExpr.evalF64(context).toInt()
            (target as MutableList<Any>)[index] = value
        } else if (target is MutableMap<*, *>) {
            val key = keyExpr.eval(context)
            (target as MutableMap<Any, Any>)[key] = value
        } else {
            throw RuntimeException("Can't assign to type ${baseExpr.returnType}")
        }
    }

    override val returnType: Type
        get() = when(baseExpr.returnType.unparameterized ?: baseExpr.returnType) {
            StrType -> StrType
            is ListType -> baseExpr.returnType.genericParameterTypes[0]
            is MapType -> baseExpr.returnType.genericParameterTypes[1]
            else -> throw IllegalArgumentException()
        }

    override fun children(): List<Node> = listOf(baseExpr, keyExpr)

    override fun eval(context: LocalRuntimeContext): Any {
        val base = baseExpr.eval(context)
        if (base is Map<*,*>) {
            val key = keyExpr.eval(context)
            return (base as Map<Any, Any>)[key]!!
        } else {
            var index = keyExpr.evalI64(context).toInt()
            if (base is String) {
                if (index < 0) {
                    index = base.length + index
                }
                return base.substring(index, index + 1)
            } else {
                val list = base as List<Any>
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