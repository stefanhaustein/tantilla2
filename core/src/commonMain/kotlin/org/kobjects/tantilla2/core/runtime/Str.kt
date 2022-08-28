package org.kobjects.tantilla2.core.runtime

import org.kobjects.tantilla2.core.AnyType
import org.kobjects.tantilla2.core.classifier.NativeStructDefinition
import org.kobjects.tantilla2.core.function.Parameter

object Str : NativeStructDefinition(
    RootScope,
    "str",
    "A character String. Use the constructor to convert a value to its string representation.",
    ctorParams = listOf(Parameter("value", AnyType, defaultValueExpression = org.kobjects.greenspun.core.Str.Const(""))),
    ctor = {
        it[0].toString()
    }
)  {


}