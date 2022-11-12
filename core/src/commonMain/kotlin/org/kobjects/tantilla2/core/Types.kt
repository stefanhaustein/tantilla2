package org.kobjects.tantilla2.core

import org.kobjects.tantilla2.core.builtin.VoidType
import org.kobjects.tantilla2.core.node.*

fun Type.commonType(other: Type): Type =
   if (other.isAssignableFrom(this)) {
       other
   } else if (isAssignableFrom(other)) {
       this
   } else {
       AnyType
   }

fun commonType(types: List<Type>): Type {
    if (types.isEmpty()) {
        return VoidType
    }
    var result = types[0]
    for (i in 1 until types.size) {
        result = result.commonType(types[i])
    }
    return result
}


val Any?.dynamicType: Type
    get() = when (this) {
        null -> VoidType
        is Typed -> type
        is Double -> org.kobjects.tantilla2.core.builtin.FloatType
        is Long -> org.kobjects.tantilla2.core.builtin.IntType
        is Type -> MetaType(this)
        is String -> org.kobjects.tantilla2.core.builtin.StrType
        else -> throw IllegalArgumentException("Can't determine type of $this")
    }
