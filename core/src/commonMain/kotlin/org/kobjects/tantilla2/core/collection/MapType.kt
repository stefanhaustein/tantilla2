package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.IntType
import org.kobjects.tantilla2.core.type.Type

open class MapType(
    val keyType: Type,
    val valueType: Type,
    name: String = "Map",
    docString: String = "An immutable map of keys to values.",
    override val unparameterized: MapType? = null,
    ctor: (LocalRuntimeContext) -> Any = { LinkedHashMap<Any, Any>() },
) : NativeStructDefinition(
    null,
    name,
    docString,
    ctor,
), CollectionType {

    override val genericParameterTypes: List<Type> = listOf(keyType, valueType)

    override fun withGenericsResolved(typeList: List<Type>) =
        MapType(
            typeList[0],
            typeList[1],
            unparameterized = unparameterized ?: this)

    override fun isAssignableFrom(other: Type) =
        other is MapType && other.keyType == keyType && other.valueType == valueType

    override fun equals(other: Any?): Boolean =
        other is MapType && isAssignableFrom(other) && other !is MutableMapType


    init {
        defineMethod(
            "len", "Returns the length of the list",
            IntType
        ) {
            (it[0] as Map<*,*>).size.toLong()
        }


        defineMethod("values", "Returns a list of the values contained in this map.",
            ListType(valueType)
        ) {
            (it[0] as Map<Any,Any>).values.toList()
        }
    }
}