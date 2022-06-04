package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.*
import org.kobjects.tantilla2.core.node.TantillaNode
import org.kobjects.tantilla2.core.runtime.Void

val Evaluable<*>.returnType: Type
    get() = when(this) {
        is TantillaNode -> returnType

        is F64.Cmp -> org.kobjects.tantilla2.core.runtime.Bool
        is F64.Binary -> org.kobjects.tantilla2.core.runtime.F64
        is F64.Const -> org.kobjects.tantilla2.core.runtime.F64
        is F64.Unary -> org.kobjects.tantilla2.core.runtime.F64

        is I64.Cmp -> org.kobjects.tantilla2.core.runtime.Bool
        is I64.Binary -> org.kobjects.tantilla2.core.runtime.I64
        is I64.Unary -> org.kobjects.tantilla2.core.runtime.I64
        is I64.Const -> org.kobjects.tantilla2.core.runtime.I64

        is Str.Const -> org.kobjects.tantilla2.core.runtime.Str

        else -> throw IllegalArgumentException("Can't determine return type for unrecognized greenspun expression: $this")
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
        return Void
    }
    var result = types[0]
    for (i in 1 until types.size) {
        result = result.commonType(types[i])
    }
    return result
}


val Any?.dynamicType: Type
    get() = when (this) {
        null -> Void
        is Typed -> type
        is Double -> org.kobjects.tantilla2.core.runtime.F64
        is Long -> org.kobjects.tantilla2.core.runtime.I64
        is Type -> MetaType(this)
        is String -> org.kobjects.tantilla2.core.runtime.Str
        else -> throw IllegalArgumentException("Can't determine type of $this")
    }
