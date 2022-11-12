package org.kobjects.tantilla2.core.builtin

import org.kobjects.tantilla2.core.AnyType
import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.node.StrNode

object StrType : NativeStructDefinition(
    null,
    "str",
    "A character String. Use the constructor to convert a value to its string representation.",
    ctorParams = listOf(Parameter("value", AnyType, defaultValueExpression = StrNode.Const(""))),
    ctor = {
        it[0].toString()
    }
)  {


}