package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Type

open class MetaType(open val wrapped: Type) : Type {
    override fun toString(): String = "Meta<$wrapped>"
}