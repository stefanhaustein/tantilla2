package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.type.VoidType

class LocalRuntimeContext(
    val globalRuntimeContext: GlobalRuntimeContext,
    count: Int = 0,
    initializer: (Int) -> Any = { VoidType.None },
    val closure: LocalRuntimeContext? = null
) {
    val variables = MutableList(count, initializer)

    operator fun get(i: Int) = variables[i]

    fun f64(i: Int) = (variables[i] as Number).toDouble()
    fun i64(i: Int) = (variables[i] as Number).toLong()
    fun i32(i: Int) = (variables[i] as Number).toInt()
    fun str(i: Int) = variables[i] as String

    fun checkState(node: Evaluable) {
        if (globalRuntimeContext.stopRequested) {
            throw ProgramStoppedException(node)
        }
    }

    fun setSize(size: Int) {
        while (variables.size > size) {
            variables.removeAt(variables.size - 1)
        }
        while (variables.size < size) {
            variables.add(VoidType.None)
        }
    }

}
