package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.classifier.NativeTraitMethodDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.NativeFunctionDefinition
import org.kobjects.tantilla2.core.type.BoolType
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.TypeVariable

class IteratorTrait(
    val elementType: Type = TypeVariable("T")
) : TraitDefinition(null,"Iterator", "Trait for traversing a set of elements.", listOf(
    elementType
)) {
    override fun withGenericsResolved(genericTypeMap: GenericTypeMap): Type {
        return IteratorTrait(genericTypeMap.resolve(elementType))
    }

    init {
        add(NativeTraitMethodDefinition(
            this,
            "has_next",
            "True if more items are available",
            FunctionType.Impl(BoolType, listOf())))

        add(NativeTraitMethodDefinition(
            this,
           "next",
           "Returns the next item and advances.",
            FunctionType.Impl(elementType, listOf())))
    }

}