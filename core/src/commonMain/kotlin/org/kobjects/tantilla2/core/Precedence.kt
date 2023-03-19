package org.kobjects.tantilla2.core

object Precedence {
    const val DOT = 15
    const val BRACKET = 14
    const val POW = 13
    const val UNARY = 12
    const val MULDIV = 11
    const val PLUSMINUS = 10
    const val BITWISE_SHIFT = 9
    const val BITWISE_AND = 8
    const val BITWISE_XOR = 7
    const val BITWISE_OR = 6
    const val RELATIONAL = 5
    const val LOGICAL_NOT = 4
    const val LOGICAL_AND = 3
    const val LOGICAL_OR = 2
    const val FOR_IN = 1
}