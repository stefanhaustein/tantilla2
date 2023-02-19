package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.LocalRuntimeContext

class NativeAdapter<T>(val instance: T, val vmt: (T, Int, LocalRuntimeContext) -> Any): Adapter {
    override fun evalMethod(index: Int, context: LocalRuntimeContext) = vmt(instance, index, context)


}