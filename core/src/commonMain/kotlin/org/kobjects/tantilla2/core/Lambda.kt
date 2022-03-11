package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.parser.Parser
import org.kobjects.tantilla2.parser.TantillaTokenizer

open class Lambda (
    val type: FunctionType,
    val impl: (RuntimeContext) -> Any?
)  {
    fun eval(context: RuntimeContext) = impl(context)
}