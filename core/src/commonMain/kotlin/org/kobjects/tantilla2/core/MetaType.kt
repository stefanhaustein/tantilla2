package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Type

class MetaType(val wrapped: Type) : Type {
    override val name: String
        get() = "Meta<$wrapped>"
}