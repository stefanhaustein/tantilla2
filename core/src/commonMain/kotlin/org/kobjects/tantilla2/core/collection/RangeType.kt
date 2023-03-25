package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.classifier.NativeTypeDefinition
import org.kobjects.tantilla2.core.scope.AbsoluteRootScope
import org.kobjects.tantilla2.core.scope.Scope
import org.kobjects.tantilla2.core.type.IntType


object RangeType : NativeTypeDefinition(null, "Range", "A range of integers"), CollectionType {

    override fun added(scope: Scope) {
        defineMethod(
            "iterator",
            "Returns an iterator for this range",
            AbsoluteRootScope.iteratorTrait.withGenericsResolved(listOf(IntType))
        ) {
            IteratorTrait.createAdapter((it[0] as LongRange).iterator())
        }
    }


}