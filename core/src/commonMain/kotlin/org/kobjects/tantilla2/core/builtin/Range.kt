package org.kobjects.tantilla2.core.builtin

import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.Typed

data class Range(val start: Long, val end: Long) : Typed, Iterable<Long> {
    override val type: Type
        get() = RangeType

    override fun iterator(): Iterator<Long> {
        return object : Iterator<Long> {
            var current = start

            override fun hasNext() = current < end

            override fun next() = current++
        }
    }
}