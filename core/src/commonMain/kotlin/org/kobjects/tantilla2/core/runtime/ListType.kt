package org.kobjects.tantilla2.core.runtime

import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.classifier.NativeClassDefinition
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.typeName

class ListType(
    val elementType: Type,
) : NativeClassDefinition("List[${elementType.typeName}]", RootScope) {

    fun empty() = TypedList(this)

    fun create(size: Int, init: (Int) -> Any?) = TypedList(this, MutableList(size, init))

    init {
        defineNative("len", "Returns the length of the list",
            org.kobjects.tantilla2.core.runtime.F64, Parameter("self", this)) {
            (it[0] as TypedList).size
        }
    }
}