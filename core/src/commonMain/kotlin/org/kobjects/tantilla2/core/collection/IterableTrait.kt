package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.scope.AbsoluteRootScope
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.TypeParameter

class IterableTrait(
) : TraitDefinition(null,"Iterable", "Is able to provide an iterator", listOf(ELEMENT_TYPE)
) {

    init {
        defineMethod(
            "iterator",
            "True if more items are available",
            AbsoluteRootScope.iteratorTrait.withGenericsResolved(listOf(ELEMENT_TYPE)))
    }

    companion object {
        val ELEMENT_TYPE = TypeParameter("T")
    }

}