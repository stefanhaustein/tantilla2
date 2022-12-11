package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.Typed

data class Range(val start: Long, val end: Long) : Typed, TypedCollection {
    override val type: Type
        get() = RangeType


    override fun iterator(): Iterator<Long> {
        return object : Iterator<Long> {
            var current = start

            override fun hasNext() = current < end

            override fun next() = current++
        }
    }

    override fun contains(element: Any): Boolean {
        if (element !is Long) {
            return false
        }
        return element >= start && element < end
    }
}