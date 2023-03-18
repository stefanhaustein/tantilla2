package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.IntType
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.TypeParameter

open class MapType(
    name: String = "Map",
    docString: String = "An immutable map of keys to values.",
    ctor: (LocalRuntimeContext) -> Any = { LinkedHashMap<Any, Any>() },
) : NativeStructDefinition(
    null,
    name,
    docString,
    ctor,
), CollectionType {

    override val genericParameterTypes = listOf(KEY_TYPE, VALUE_TYPE)

    init {
        defineMethod(
            "len", "Returns the length of the list",
            IntType
        ) {
            (it[0] as Map<*,*>).size.toLong()
        }

        defineMethod("values", "Returns a list of the values contained in this map.",
            ListType().withGenericsResolved(listOf(VALUE_TYPE))
        ) {
            (it[0] as Map<Any,Any>).values.toList()
        }
    }

    companion object {
        val KEY_TYPE = TypeParameter("K")
        val VALUE_TYPE = TypeParameter("V")
    }
}