package org.kobjects.tantilla2.core.type

interface GenericType : Type {
    val genericParameterTypes: List<Type>
    fun create(types: List<Type>): Type
}