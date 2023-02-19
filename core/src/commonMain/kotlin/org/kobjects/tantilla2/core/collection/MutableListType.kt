package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.node.expression.IntNode
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.IntType
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.NoneType

class MutableListType(
    elementType: Type,
    unparameterized: MutableListType? = null
) : ListType(
    elementType,
    "MutableList",
    "A mutable list of elements.",
    unparameterized,
    {  (it.get(0) as List<*>).toMutableList() }
) {
    override fun withGenericsResolved(genericTypeMap: GenericTypeMap) = MutableListType(
        genericTypeMap.resolve(elementType), this)

    override fun create(size: Int, init: (Int) -> Any) = MutableList(size, init)

    override fun equals(other: Any?): Boolean =
        other is MutableListType && other.elementType == elementType

    init {
        defineMethod("append", "Appends an element to the list",
            NoneType,
            Parameter("value", elementType)) {
            (it[0] as MutableList<Any>).add(it[1])
        }

        defineMethod("clear", "Remove all elements from this list.",
            NoneType
        ) {
            (it[0] as MutableList<*>).clear()
        }

        defineMethod("insert", "Inserts an value into the list at the given index",
            NoneType,
            Parameter("index", IntType),
            Parameter("value", elementType)
        ) {
            (it[0] as MutableList<Any>).add(it.i32(1), it[2])
        }

        defineMethod("pop", "Remove the last element in this list and return it.",
            elementType,
            Parameter("index", IntType, IntNode.Const(-1))
            ) {
            val list = it[0] as MutableList<Any>
            val index = it.i32(1)
            list.removeAt(if (index < 0) list.size + index else index)
        }

        defineMethod("sort", "Sort this list in place.",
            NoneType
        ) {
            (it[0] as (MutableList<Comparable<Any>>)).sort()
        }

        defineNativeFunction(
            "init",
            "Create a mutable list of the given size, filled using the function parameter",
            this,
            Parameter("len", IntType),
            Parameter("fill", FunctionType.Impl(elementType, listOf(Parameter("index", IntType)))),
        ) { context ->
            val size = context.i32(0)
            val fn = context[1] as Callable
            val functionContext = LocalRuntimeContext(
                context.globalRuntimeContext,
                fn,
            )
            MutableList(size) {
                functionContext.variables[0] = it.toLong()
                fn.eval(functionContext)
            }
        }

    }
}