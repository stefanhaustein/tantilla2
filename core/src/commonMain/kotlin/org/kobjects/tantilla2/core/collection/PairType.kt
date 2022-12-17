package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.type.GenericType
import org.kobjects.tantilla2.core.type.IntType
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.VoidType

open class PairType(
    val typeA: Type,
    val typeB: Type,
) : NativeStructDefinition(
    null,
    "Pair",
    "An immutable pair of two values",
    listOf(Parameter("a", typeA), Parameter("b", typeB)),
    { TypedPair(typeA, typeB, it.get(0)!!, it.get(1)!!) },
), CollectionType, GenericType {

    override fun serializeType(writer: CodeWriter, scope: Scope?) {
        writer.append(name).append("[").appendType(typeA, scope).append(", ")
            .appendType(typeB, scope).append("]")
    }

    override val genericParameterTypes: List<Type> = listOf(typeA, typeB)

    override fun create(types: List<Type>) = PairType(types[0], types[1])

    override fun equals(other: Any?): Boolean =
        other is PairType && other.typeA == typeA && other.typeB == typeB

    init {
        defineMethod(
            "a", "Element a of the pair",
            typeA
        ) {
            (it[0] as TypedPair).a
        }

        defineMethod(
            "b", "Element b of the pair",
            typeB
        ) {
            (it[0] as TypedPair).b
        }
    }
}