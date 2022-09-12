package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.*
import org.kobjects.tantilla2.core.node.TantillaNode
import org.kobjects.tantilla2.core.builtin.VoidType

val Evaluable<*>.returnType: Type
    get() = when(this) {
        is TantillaNode -> returnType

        is F64.Cmp -> org.kobjects.tantilla2.core.builtin.BoolType
        is F64.Binary -> org.kobjects.tantilla2.core.builtin.FloatType
        is F64.Const -> org.kobjects.tantilla2.core.builtin.FloatType
        is F64.Unary -> org.kobjects.tantilla2.core.builtin.FloatType

        is I64.Cmp -> org.kobjects.tantilla2.core.builtin.BoolType
        is I64.Binary -> org.kobjects.tantilla2.core.builtin.IntType
        is I64.Unary -> org.kobjects.tantilla2.core.builtin.IntType
        is I64.Const -> org.kobjects.tantilla2.core.builtin.IntType

        is Str.Const -> org.kobjects.tantilla2.core.builtin.StrType

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
