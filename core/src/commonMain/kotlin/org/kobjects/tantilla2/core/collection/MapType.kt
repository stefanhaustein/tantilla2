package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.type.GenericType
import org.kobjects.tantilla2.core.type.IntType
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.VoidType

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
    listOf(),
    ctor,
), CollectionType, GenericType {

    override fun serializeType(writer: CodeWriter, scope: Scope?) {
        writer.append(name).append("[").appendType(keyType, scope).append(", ")
            .appendType(valueType, scope).append("]")
    }

    override val genericParameterTypes: List<Type> = listOf(keyType, valueType)

    override fun create(types: List<Type>) = MapType(types[0], types[1])

    override fun isAssignableFrom(other: Type) =
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