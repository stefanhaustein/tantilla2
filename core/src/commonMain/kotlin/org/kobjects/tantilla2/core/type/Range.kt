package org.kobjects.tantilla2.core.type

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