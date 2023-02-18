package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.classifier.NativeTraitMethodDefinition
import org.kobjects.tantilla2.core.classifier.NativeTypeDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.type.BoolType
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.TypeVariable

class IteratorTrait(
    val elementType: Type = TypeVariable("T"),
    override val unparameterized: IteratorTrait? = null,
) : TraitDefinition(null, "Iterator", "Able to iterate a sequence of elements", listOf(elementType)) {


    fun withElementType(elementType: Type) = IteratorTrait(elementType, unparameterized ?: this)

    override fun withGenericsResolved(genericTypeMap: GenericTypeMap) =
        withElementType(genericTypeMap.resolve(elementType))

    init {
        if (elementType !is TypeVariable && unparameterized == null) {
            throw IllegalStateException()
        }

        defineMethod(
            "has_next",
            "True if more items are available",
            BoolType)

        defineMethod(
           "next",
           "Returns the next item and advances.",
            elementType)
    }

}