package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.function.Parameter

open class ListType(
    val elementType: Type,
    name: String = "List[${elementType.typeName}]"
) : NativeStructDefinition(null, name) {

    open fun create(size: Int, init: (Int) -> Any?) = TypedList(this, MutableList(size, init))

    init {
        defineNativeFunction("len", "Returns the length of the list",
            org.kobjects.tantilla2.core.type.IntType, Parameter("self", this)) {
            (it[0] as TypedList).size.toLong()
        }
    }

    override fun equals(other: Any?): Boolean =
        other is ListType && other.elementType == elementType && other !is MutableListType
}