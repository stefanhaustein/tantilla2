package org.kobjects.tantilla2.core.runtime

import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.core.Typed

data class Range(val start: Double, val end: Double) : Typed {
    override val type: Type
        get() = RangeType
}