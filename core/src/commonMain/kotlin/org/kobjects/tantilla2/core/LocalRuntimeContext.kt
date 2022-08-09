package org.kobjects.tantilla2.core

class LocalRuntimeContext(
    val globalRuntimeContext: GlobalRuntimeContext,
    count: Int = 0,
    initializer: (Int) -> Any? = { null },
    val closure: LocalRuntimeContext? = null
) {
    val variables = MutableList(count, initializer)

    operator fun get(i: Int) = variables[i]

    fun f64(i: Int) = (variables[i] as Number).toDouble()
    fun i64(i: Int) = variables[i] as Long
    fun str(i: Int) = variables[i] as String
}
