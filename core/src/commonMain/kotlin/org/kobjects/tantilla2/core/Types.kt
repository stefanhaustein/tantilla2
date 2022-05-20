package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.*

val Evaluable<*>.type: Type
    get() = when(this) {
        is F64.Binary -> Type.F64
        is F64.Const -> Type.F64
        is F64.Unary -> Type.F64
        is I64.Binary -> Type.I64
        is I64.Unary -> Type.I64
        is I64.Const -> Type.I64
        else -> throw IllegalArgumentException("Unrecognized type: ${this::class}")
    }


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
        return Type.Void
    }
    var result = types[0]
    for (i in 1 until types.size) {
        result = result.commonType(types[i])
    }
    return result
}


val Any?.type: Type
    get() = when (this) {
        null -> Type.Void
        is Typed -> type
        is Double -> Type.F64
        is Long -> Type.I64
        is Type -> MetaType(this)
        is String -> Type.Str
        else -> throw IllegalArgumentException("Can't determine type of $this")
    }
