package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.definition.ContextOwner
import org.kobjects.tantilla2.core.type.VoidType

class LocalRuntimeContext(
    val globalRuntimeContext: GlobalRuntimeContext,
    val scope: ContextOwner,
    initializer: (Int) -> Any = { VoidType.None },
) {
    val variables = MutableList(scope.dynamicScopeSize, initializer)

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

    override fun toString() = scope.toString(this)
}
