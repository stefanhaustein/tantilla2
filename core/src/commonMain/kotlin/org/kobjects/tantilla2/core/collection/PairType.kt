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
    { TypedPair(firstType, secondType, it.get(0)!!, it.get(1)!!) },
    Parameter("a", firstType),
    Parameter("b", secondType),
), CollectionType {

    override val genericParameterTypes: List<Type> = listOf(firstType, secondType)

    override fun withGenericsResolved(genericTypeMap: GenericTypeMap) =
        PairType(genericTypeMap.resolve(firstType), genericTypeMap.resolve(secondType), this)

    override fun equals(other: Any?): Boolean =
        other is PairType && other.firstType == firstType && other.secondType == secondType

    init {
        defineMethod(
            "a", "Element a of the pair",
            firstType
        ) {
            (it[0] as TypedPair).a
        }

        defineMethod(
            "b", "Element b of the pair",
            secondType
        ) {
            (it[0] as TypedPair).b
        }
    }
}