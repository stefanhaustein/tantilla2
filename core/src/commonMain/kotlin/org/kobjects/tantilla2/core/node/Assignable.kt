package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.LocalRuntimeContext

interface Assignable : TantillaNode {

    fun assign(context: LocalRuntimeContext, value: Any?)
}