package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.TraitMethodBody
import org.kobjects.tantilla2.core.classifier.NativeTraitMethodDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.definition.Definition
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.NativeFunctionDefinition
import org.kobjects.tantilla2.core.type.BoolType
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.TypeVariable

class IterableTrait(
    val elementType: Type = TypeVariable("T")
) : TraitDefinition(null,"Iterable", "Is able to provide an iterator", listOf(
    elementType
)) {
    override fun withGenericsResolved(genericTypeMap: GenericTypeMap): Type {
        return IterableTrait(genericTypeMap.resolve(elementType))
    }

    init {
        add(
            NativeTraitMethodDefinition(
            this,
            "iterator",
            "True if more items are available",
            FunctionType.Impl(IteratorTrait(elementType), listOf()))
        )
    }

}