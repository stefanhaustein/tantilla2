package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Type
import org.kobjects.tantilla2.parser.Parser
import org.kobjects.tantilla2.parser.TantillaTokenizer

interface Callable : Typed {
    fun eval(context: RuntimeContext): Any?
}