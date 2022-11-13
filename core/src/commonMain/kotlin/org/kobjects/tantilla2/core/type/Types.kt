package org.kobjects.tantilla2.core.type

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
        is Double -> FloatType
        is Long -> IntType
        is Type -> MetaType(this)
        is String -> StrType
        else -> throw IllegalArgumentException("Can't determine type of $this")
    }
