package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.definition.AbsoluteRootScope
import org.kobjects.tantilla2.core.definition.SystemRootScope
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.TypeVariable

class IterableTrait(
    val elementType: Type = TypeVariable("T"),
    override val unparameterized: IterableTrait? = null,
) : TraitDefinition(null,"Iterable", "Is able to provide an iterator", listOf(
    elementType
)) {

    fun withElementType(elementType: Type) = IterableTrait(
        elementType, unparameterized ?: this)

    override fun withGenericsResolved(genericTypeMap: GenericTypeMap) =
        withElementType(genericTypeMap.resolve(elementType))

    init {
        defineMethod(
            "iterator",
            "True if more items are available",
            AbsoluteRootScope.iteratorTrait.withElementType(elementType))
    }

}