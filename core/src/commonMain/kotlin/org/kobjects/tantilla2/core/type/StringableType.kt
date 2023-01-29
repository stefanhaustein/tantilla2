package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.classifier.NativeTypeDefinition
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.node.expression.IntNode
import org.kobjects.tantilla2.core.node.expression.StrNode
import stringify

object StringableType : NativeTypeDefinition(
        null,
        "Stringable",
        "A type that can be converted to a string (= any type basically).",
    ) {
    init {
        defineMethod(
            "toString",
            "Converts this object to a string.",
            StrType
        ) {
            it[0].stringify()
        }

    }

    override fun isAssignableFrom(type: Type, allowAs: Boolean) = true
}