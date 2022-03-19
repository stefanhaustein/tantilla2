package org.kobjects.tantilla2.core

import org.kobjects.greenspun.core.Evaluable

class Apply(
    val callable: Evaluable<RuntimeContext>,
    val parameters: List<Evaluable<RuntimeContext>>
) : Evaluable<RuntimeContext> {
    override fun eval(context: RuntimeContext): Any? {
        val function = callable.eval(context) as Lambda
        val functionContext = RuntimeContext(MutableList<Any?>(function.parameters.size) {

            if (it < parameters.size) {
                println("Evaluating ${parameters[it]}")
                val result = parameters[it].eval(context)
                println("Result $result")
                result
            } else null
        })
        return function.eval(functionContext)
    }

    override fun children(): List<Evaluable<RuntimeContext>> = List(parameters.size + 1) {
       if (it == 0) callable else parameters[it - 1]
    }

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>): Evaluable<RuntimeContext> =
        Apply(newChildren[0], newChildren.subList(1, newChildren.size))

    override val type
        get() = (callable.type as FunctionType).returnType

    override fun toString(): String =
        "${callable}(${parameters.joinToString(", ")})"
}