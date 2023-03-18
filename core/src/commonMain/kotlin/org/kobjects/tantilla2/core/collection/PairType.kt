package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.TypeParameter

private val FIRST = TypeParameter("F")
private val SECOND = TypeParameter("S")
private val TYPE_LIST = listOf(FIRST, SECOND)

object PairType : NativeStructDefinition(
    null,
    "Pair",
    "An immutable pair of two values",
    { Pair(it.get(0), it.get(1)) },
    Parameter("first", FIRST),
    Parameter("second", SECOND),
), CollectionType {

    override val genericParameterTypes: List<Type>
        get() = TYPE_LIST

    init {
        defineMethod(
            "first", "First element the pair",
            FIRST
        ) {
            (it[0] as Pair<Any, Any>).first
        }

        defineMethod(
            "second", "Second element of the pair",
            SECOND
        ) {
            (it[0] as Pair<Any, Any>).second
        }
    }
}