package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.*

fun Type.commonType(other: Type): Type =
   if (other.isAssignableFrom(this)) {
       other
   } else if (isAssignableFrom(other)) {
       this
   } else {
       AnyType
   }

fun commonType(types: List<Type>): Type {
    if (types.size == 0) {
        return Void
    }
    var result = types[0]
    for (i in 1 until types.size) {
        result = result.commonType(types[i])
    }
    return result
}


val Any?.type: Type
    get() = when (this) {
        null -> Void
        is Typed -> type
        is Double -> F64
        is Long -> I64
        is Type -> MetaType(this)
        is String -> Str
        else -> throw IllegalArgumentException("Can't determine type of $this")
    }
