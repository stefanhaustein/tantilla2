package org.kobjects.tantilla2.core.type

import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.node.expression.StrNode

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