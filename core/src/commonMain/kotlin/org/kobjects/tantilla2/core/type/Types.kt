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
        return NoneType
    }
    var result = types[0]
    for (i in 1 until types.size) {
        result = result.commonType(types[i])
    }
    return result
}

