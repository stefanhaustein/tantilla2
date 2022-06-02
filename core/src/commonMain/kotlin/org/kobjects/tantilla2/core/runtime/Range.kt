package org.kobjects.tantilla2.core.runtime

import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.Typed

data class Range(val start: Double, val end: Double) : Typed, Iterable<Double> {
    override val type: Type
        get() = RangeType

    override fun iterator(): Iterator<Double> {
        return object : Iterator<Double> {
            var current = start

            override fun hasNext() = current < end

            override fun next() = current++
        }
    }
}