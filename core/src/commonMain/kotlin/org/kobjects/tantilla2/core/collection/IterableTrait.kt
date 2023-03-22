package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.scope.AbsoluteRootScope
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.TypeParameter

class IterableTrait(
     elementType: Type = TypeParameter("T"),
    val unparameterized: IterableTrait? = null,
) : TraitDefinition(null,"Iterable", "Is able to provide an iterator", listOf(elementType)
) {


    fun withElementType(elementType: Type) = IterableTrait(
        elementType, unparameterized() as IterableTrait)

    override fun withGenericsResolved(genericTypeList: List<Type>) =
        withElementType(genericTypeList[0])


    override fun unparameterized(): TraitDefinition = unparameterized ?: this


    init {
        defineMethod(
            "iterator",
            "True if more items are available",
            AbsoluteRootScope.iteratorTrait.withElementType(elementType))
    }

}