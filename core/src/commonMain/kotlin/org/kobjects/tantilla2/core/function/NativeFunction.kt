package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.RuntimeContext

class NativeFunction(
    override val type: FunctionType,
    val body: (RuntimeContext) -> Any?,
) : Callable {

    /*override val name: String
        get() = "(${type.parameters}) -> ${type.returnType}"
*/
    override fun eval(context: RuntimeContext) = body(context)

    override fun toString() = "$type:\n  $body"
}