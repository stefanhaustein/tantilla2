package org.kobjects.tantilla2.core

interface Type {

    fun isAssignableFrom(type: Type) = type == this


    object Void : Type

    object F64 : Type

    object I64 : Type

    object Str : Type

}