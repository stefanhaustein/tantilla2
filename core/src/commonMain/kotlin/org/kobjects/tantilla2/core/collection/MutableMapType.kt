package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.Type

class MutableMapType(
    keyType: Type,
    valueType: Type,
    unparameterized: MutableMapType? = null,
) : MapType(
    keyType,
    valueType,
    "MutableMap",
    "A mutable map.",
    unparameterized,
     { mutableMapOf<Any, Any>() },
) {

    override fun equals(other: Any?): Boolean =
        other is MutableMapType && other.keyType == keyType && other.valueType == valueType

    override fun withGenericsResolved(typeList: List<Type>) =
        MutableMapType(typeList[0], typeList[1], this)
}