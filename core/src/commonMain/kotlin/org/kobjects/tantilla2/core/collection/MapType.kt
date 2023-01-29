package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.type.IntType
import org.kobjects.tantilla2.core.type.Type

open class MapType(
    val keyType: Type,
    val valueType: Type,
    name: String = "Map",
    docString: String = "An immutable map of keys to values.",
    ctor: (LocalRuntimeContext) -> Any = { TypedMap(keyType, valueType) },
) : NativeStructDefinition(
    null,
    name,
    docString,
    ctor,
), CollectionType {

    override val genericParameterTypes: List<Type> = listOf(keyType, valueType)

    override fun withGenericsResolved(types: List<Type>) = MapType(types[0], types[1])

    override fun isAssignableFrom(other: Type, allowAs: Boolean) =
        other is MapType && other.keyType == keyType && other.valueType == valueType

    override fun equals(other: Any?): Boolean =
        other is MapType && isAssignableFrom(other) && other !is MutableMapType


    init {
        defineMethod(
            "len", "Returns the length of the list",
            IntType
        ) {
            (it[0] as TypedMap).size.toLong()
        }


        defineMethod("values", "Returns a list of the values contained in this map.",
            ListType(valueType)
        ) {
            TypedList(valueType, (it[0] as TypedMap).data.values.toList())
        }
    }
}