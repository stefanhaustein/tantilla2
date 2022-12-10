package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.type.Type

interface Generic {
    val genericParameterTypes: List<Type>
    fun create(types: List<Type>): Type
}