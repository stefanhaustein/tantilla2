package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.classifier.NativeTypeDefinition
import org.kobjects.tantilla2.core.definition.AbsoluteRootScope
import org.kobjects.tantilla2.core.definition.Scope
import org.kobjects.tantilla2.core.type.IntType
import org.kobjects.tantilla2.core.type.Type


object RangeType : NativeTypeDefinition(null, "Range", "A range of integers"), CollectionType {

    override fun added(scope: Scope) {
        defineMethod(
            "iterator",
            "Returns an iterator for this range",
            AbsoluteRootScope.iteratorTrait.withElementType(IntType)
        ) {
            IteratorTrait.createAdapter((it[0] as LongRange).iterator())
        }
    }


}