package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.type.IntType
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.VoidType

class MutableListType(
    elementType: Type
) : ListType(
    elementType,
    "MutableList",
    "A mutable list of elements.",
    { MutableTypedList(elementType, (it.get(0) as TypedList).data.toMutableList()) }
) {
    override fun create(types: List<Type>) = MutableListType(types.first())

    override fun create(size: Int, init: (Int) -> Any) = MutableTypedList(this, MutableList(size, init))

    override fun equals(other: Any?): Boolean =
        other is MutableListType && other.elementType == elementType

    init {
        defineMethod("append", "Appends an element to the list",
            VoidType,
            Parameter("value", elementType)) {
            (it[0] as MutableTypedList).data.add(it[1]!!)
        }

        defineMethod("clear", "Remove all elements from this list.",
            VoidType
        ) {
            (it[0] as MutableTypedList).data.clear()
        }

        defineMethod("insert", "Inserts an value into the list at the given index",
            VoidType,
            Parameter("index", IntType),
            Parameter("value", elementType)
        ) {
            (it[0] as MutableTypedList).data.add(it.i32(1), it[2]!!)
        }

        defineMethod("pop", "Remove the last element in this list and return it.",
            elementType) {
            (it[0] as MutableTypedList).data.removeLast()
        }

        defineMethod("sort", "Sort this list in place.",
            VoidType
        ) {
            ((it[0] as MutableTypedList).data as (MutableList<Comparable<Any>>)).sort()
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
                fn.scopeSize,
                closure = fn.closure,
            )
            MutableTypedList(elementType, MutableList(size) {
                functionContext.variables[0] = it.toLong()
                fn.eval(functionContext)!!
            } )
        }

    }
}