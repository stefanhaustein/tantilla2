package org.kobjects.tantilla2.core

object Precedence {
    const val DOT = 14
    const val BRACKET = 13
    const val POW = 12
    const val UNARY = 11
    const val MULDIV = 10
    const val PLUSMINUS = 9
    const val BITWISE_SHIFT = 8
    const val BITWISE_AND = 7
    const val BITWISE_XOR = 6
    const val BITWISE_OR = 5
    const val RELATIONAL = 4
    const val LOGICAL_NOT = 3
    const val LOGICAL_AND = 2
    const val LOGICAL_OR = 1
}