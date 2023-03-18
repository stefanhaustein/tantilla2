package org.kobjects.tantilla2.core.collection

import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.type.GenericTypeMap
import org.kobjects.tantilla2.core.type.IntType
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.TypeParameter

open class SetType(
    name: String = "Set",
    docString: String = "An immutable set of elements.",
    ctor:  (LocalRuntimeContext) -> Any = { (it.get(0) as List<Any>).toSet() }
) : NativeStructDefinition(
    null,
    name,
    docString,
    ctor,
    Parameter("elements", ELEMENT_TYPE_PARAMETER, isVararg = true),
), CollectionType {

    init {
        defineMethod("len", "Returns the size of this set", IntType) {
            (it[0] as Set<*>).size.toLong()
        }

    }

    override val genericParameterTypes: List<Type>
        get() = GENERIC_PARAMETER_YPES

    companion object {
        val ELEMENT_TYPE_PARAMETER = TypeParameter("E")
        val GENERIC_PARAMETER_YPES = listOf(ELEMENT_TYPE_PARAMETER)
    }
}