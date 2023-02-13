package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.classifier.NativeTraitMethodDefinition
import org.kobjects.tantilla2.core.classifier.NativeTypeDefinition
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.type.BoolType
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.TypeVariable

class IteratorType(
    val elementType: Type = TypeVariable("T"),
    override val unparameterized: IteratorType? = null,
) : NativeTypeDefinition(null, "Iterator")
     {
    override fun withGenericsResolved(genericTypeMap: GenericTypeMap): Type {
        return IteratorType(genericTypeMap.resolve(elementType), this)
    }

    init {
        defineMethod(
            "has_next",
            "True if more items are available",
            FunctionType.Impl(BoolType, listOf())) {
            (it[0] as Iterator<Any>).hasNext()
        }

        defineMethod(
           "next",
           "Returns the next item and advances.",
            FunctionType.Impl(elementType, listOf())) {
            (it[0] as Iterator<Any>).next()
        }
    }

}