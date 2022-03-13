package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable

interface Assignable : Evaluable<RuntimeContext> {

    fun assign(context: RuntimeContext, value: Any?)
}