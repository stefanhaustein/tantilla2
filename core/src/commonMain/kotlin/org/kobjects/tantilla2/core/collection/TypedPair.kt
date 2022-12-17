package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.Typed
import toLiteral

open class TypedPair(
    val typeA: Type,
    val typeB: Type,
    val a: Any,
    val b: Any
): Typed {

    override val type: Type
        get() = PairType(typeA, typeB)

    override fun hashCode(): Int {
        return a.hashCode() xor (3 * b.hashCode())
    }

    override fun equals(other: Any?): Boolean {
        return other is TypedPair && other.a == a && other.b == b
    }

    override fun toString() = "Pair(${a.toLiteral()}, ${b.toLiteral()})"
}

