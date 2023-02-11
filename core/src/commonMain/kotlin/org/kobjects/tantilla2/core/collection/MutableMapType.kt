package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.Type

class MutableMapType(
    keyType: Type,
    valueType: Type,
) : MapType(
    keyType,
    valueType,
    "MutableMap",
    "A mutable map.",
     { MutableTypedMap(keyType, valueType) },
) {

    override fun equals(other: Any?): Boolean =
        other is MutableMapType && other.keyType == keyType && other.valueType == valueType

    override fun withGenericsResolved(genericTypeMap: GenericTypeMap) =
        MutableMapType(
            genericTypeMap.resolve(keyType), genericTypeMap.resolve(valueType))
}