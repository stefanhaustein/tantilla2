package org.kobjects.tantilla2.core

class NativeFunction(
    override val type: FunctionType,
    val body: (RuntimeContext) -> Any?,
) : Lambda {

    /*override val name: String
        get() = "(${type.parameters}) -> ${type.returnType}"
*/
    override fun eval(context: RuntimeContext) = body(context)

    override fun toString() = "$type:\n  $body"
}