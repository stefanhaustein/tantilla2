package org.kobjects.tantilla2.core.runtime

import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.function.Parameter

data class ListType(
    val elementType: Type,
) : NativeStructDefinition(RootScope, "List[${elementType.typeName}]") {

    fun empty() = TypedList(this)

    fun create(size: Int, init: (Int) -> Any?) = TypedList(this, MutableList(size, init))

    init {
        defineNativeFunction("len", "Returns the length of the list",
            org.kobjects.tantilla2.core.runtime.F64, Parameter("self", this)) {
            (it[0] as TypedList).size
        }
    }
}