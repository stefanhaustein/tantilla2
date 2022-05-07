package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Type

object AnyType : Type {
    override fun toString() = "Any"

    override fun isAssignableFrom(type: Type) = true
}