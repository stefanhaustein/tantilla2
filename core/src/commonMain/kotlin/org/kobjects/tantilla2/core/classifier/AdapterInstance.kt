package org.kobjects.tantilla2.core.classifier

import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.type.NoneType

class AdapterInstance  (
    private val vmt: List<Callable>,
    private val instance: Any,
): Adapter {
    override fun evalMethod(vmtIndex: Int, context: LocalRuntimeContext): Any {
        val methodImpl = vmt[vmtIndex]
        val methodContext = LocalRuntimeContext(
            context.globalRuntimeContext,
            methodImpl,
        ) {
            if (it == 0) instance
            else if (it < context.variables.size) context.variables[it]
            else NoneType.None
        }
        return methodImpl.eval(methodContext)
    }


}