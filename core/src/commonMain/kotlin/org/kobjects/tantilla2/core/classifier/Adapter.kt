package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.LocalRuntimeContext

interface Adapter {

    fun evalMethod(index: Int, context: LocalRuntimeContext): Any
}