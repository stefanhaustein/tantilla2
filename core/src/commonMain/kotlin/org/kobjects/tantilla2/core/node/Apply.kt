package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Lambda
import org.kobjects.tantilla2.core.serialize


class Apply(
    val callable: Evaluable<RuntimeContext>,
    val parameters: List<Evaluable<RuntimeContext>>
) : Evaluable<RuntimeContext>, Serializable {
    override fun eval(context: RuntimeContext): Any? {
        val shouldBeLambda = callable.eval(context)
        if (shouldBeLambda !is Lambda) {
            throw IllegalStateException("Lambda expected; got $shouldBeLambda")
        }
        val function = shouldBeLambda as Lambda
        val functionContext = RuntimeContext(MutableList<Any?>(function.type.parameters.size) {

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

    override fun serialize(sb: StringBuilder, indent: String, parentPrcedence: Int) {
        callable.serialize(sb, indent)
        sb.append("(")
        if (parameters.size > 0) {
            parameters[0].serialize(sb, indent)
            for (i in 1 until parameters.size) {
                sb.append(", ")
                parameters[i].serialize(sb, indent)
            }
        }
        sb.append(")")
    }
}