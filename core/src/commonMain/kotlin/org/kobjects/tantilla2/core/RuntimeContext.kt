package org.kobjects.tantilla2.core

data class RuntimeContext(val variables: MutableList<Any?>) {
    operator fun get(i: Int) = variables[i]

    fun f64(i: Int) = variables[i] as Double
    fun i64(i: Int) = variables[i] as Long
    fun str(i: Int) = variables[i] as String
}
