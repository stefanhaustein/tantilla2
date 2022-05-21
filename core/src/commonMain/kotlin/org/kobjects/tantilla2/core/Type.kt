package org.kobjects.tantilla2.core

interface Type {

    fun isAssignableFrom(type: Type) = type == this


}