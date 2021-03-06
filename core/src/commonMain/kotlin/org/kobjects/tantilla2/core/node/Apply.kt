package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.returnType


class Apply(
    val callable: Evaluable<RuntimeContext>,
    val parameters: List<Evaluable<RuntimeContext>>,
    val implicit: Boolean,
    val asMethod: Boolean,
) : TantillaNode {
    override fun eval(context: RuntimeContext): Any? {
        val shouldBeLambda = callable.eval(context)
        if (shouldBeLambda !is Callable) {
            throw IllegalStateException("Lambda expected; got $shouldBeLambda")
        }
        val function = shouldBeLambda as Callable
        val functionContext = RuntimeContext(MutableList<Any?>(function.scopeSize) {
            if (it < parameters.size) {
                val result = parameters[it].eval(context)
                // println("Result $result")
                result
            } else null
        }, function.closure)
        return function.eval(functionContext)
    }

    override fun children(): List<Evaluable<RuntimeContext>> = List(parameters.size + 1) {
       if (it == 0) callable else parameters[it - 1]
    }

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>): Evaluable<RuntimeContext> =
        Apply(newChildren[0], newChildren.subList(1, newChildren.size), implicit, asMethod)

    override val returnType
        get() = (callable.returnType as FunctionType).returnType

    override fun serializeCode(sb: CodeWriter, parentPrcedence: Int) {
        val startIndex = if (!asMethod) 0 else {
            sb.appendCode(parameters[0])
            sb.append(".")
            1
        }

        sb.appendCode(callable)
        if (!implicit) {
            sb.append("(")
            if (parameters.size > startIndex) {
                sb.appendCode(parameters[startIndex])
                for (i in startIndex + 1 until parameters.size) {
                    sb.append(", ")
                    sb.appendCode(parameters[i])
                }
            }
            sb.append(")")
        }
    }

    override fun toString(): String = CodeWriter().appendCode(this).toString()
}
