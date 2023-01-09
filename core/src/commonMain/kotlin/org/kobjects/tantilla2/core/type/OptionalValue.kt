package org.kobjects.tantilla2.core.type

class OptionalValue(
    val valueType: Type,
    val value: Any?,
) : Typed {
    override val type: Type
        get() = OptionalType(valueType)
}