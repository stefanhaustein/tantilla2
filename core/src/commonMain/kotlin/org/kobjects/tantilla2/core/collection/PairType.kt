package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.Type

open class PairType(
    val firstType: Type,
    val secondType: Type,
    override val unparameterized: PairType? = null,
) : NativeStructDefinition(
    null,
    "Pair",
    "An immutable pair of two values",
    { Pair(it.get(0), it.get(1)) },
    Parameter("first", firstType),
    Parameter("second", secondType),
), CollectionType {

    override val genericParameterTypes: List<Type> = listOf(firstType, secondType)

    override fun withGenericsResolved(typeList: List<Type>) =
        PairType(typeList[0], typeList[1],  unparameterized ?: this)

    override fun equals(other: Any?): Boolean =
        other is PairType && other.firstType == firstType && other.secondType == secondType

    init {
        defineMethod(
            "first", "First element the pair",
            firstType
        ) {
            (it[0] as Pair<Any, Any>).first
        }

        defineMethod(
            "second", "Second element of the pair",
            secondType
        ) {
            (it[0] as Pair<Any, Any>).second
        }
    }
}