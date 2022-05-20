package org.kobjects.tantilla2.core

open class MetaType(open val wrapped: Type) : Type {
    override fun toString(): String = "Meta<$wrapped>"
}