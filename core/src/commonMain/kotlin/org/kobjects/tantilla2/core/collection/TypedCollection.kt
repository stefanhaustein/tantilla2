package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.type.Typed

interface TypedCollection : Typed, Iterable<Any> {

    override fun iterator(): Iterator<Any>

    fun contains(element: Any): Boolean {
        val iterator = iterator()
        while (iterator.hasNext()) {
            if (iterator.next() == element) {
                return true
            }
        }
        return false
    }

}