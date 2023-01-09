package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.function.Parameter

class OptionalType(val valueType: Type)  : NativeStructDefinition(
    null,
    "Optional",
    "A wrapper for an optional value.",
    { OptionalValue(valueType, it.get(0)) },
    Parameter("value", valueType),
) {

    init {
        defineMethod("get", "Returns the value or throws an exception if no value is present",
            valueType
        ) {
            (it[0] as OptionalValue).value ?: throw IllegalStateException("No value available.")
        }

        defineMethod("get_or_default", "Returns the value or the given default value if no value is present",
            valueType,
            Parameter("default", valueType)
        ) {
            (it[0] as OptionalValue).value ?: it[0]
        }
    }


    override val genericParameterTypes: List<Type> = listOf(valueType)

    override fun withGenericsResolved(types: List<Type>) = OptionalType(types.first())
}