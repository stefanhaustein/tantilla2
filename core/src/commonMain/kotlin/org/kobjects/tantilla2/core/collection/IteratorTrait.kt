package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.classifier.NativeAdapter
import org.kobjects.tantilla2.core.classifier.TraitDefinition
import org.kobjects.tantilla2.core.type.BoolType
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.TypeParameter

class IteratorTrait(
    val elementType: Type = TypeParameter("T"),
    override val unparameterized: IteratorTrait? = null,
) : TraitDefinition(null, "Iterator", "Able to iterate a sequence of elements", listOf(elementType)) {


    fun withElementType(elementType: Type) = IteratorTrait(elementType, unparameterized ?: this)

    override fun withGenericsResolved(genericTypeList: List<Type>) =
        withElementType(genericTypeList[0])

    init {
        if (elementType !is TypeParameter && unparameterized == null) {
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

    companion object {
        fun createAdapter(iterator: Iterator<Any>) =
            NativeAdapter(iterator) { self, index, _ ->
                when(index) {
                    0 -> self.hasNext()
                    1 -> self.next()
                    else -> throw IllegalArgumentException()
                }
            }
    }
}