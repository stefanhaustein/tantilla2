package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.RuntimeContext

interface Assignable : Evaluable<RuntimeContext> {

    fun assign(context: RuntimeContext, value: Any?)
}