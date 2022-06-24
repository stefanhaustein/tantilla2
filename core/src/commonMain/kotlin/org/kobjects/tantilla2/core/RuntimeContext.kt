package org.kobjects.tantilla2.core

data class RuntimeContext(
    val variables: MutableList<Any?>,
    val closure: RuntimeContext? = null
) {
    operator fun get(i: Int) = variables[i]

    fun f64(i: Int) = (variables[i] as Number).toDouble()
    fun i64(i: Int) = variables[i] as Long
    fun str(i: Int) = variables[i] as String
}
