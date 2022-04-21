package org.kobjects.tantilla2.function

import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.Typed

interface Callable : Typed {
    fun eval(context: RuntimeContext): Any?
}