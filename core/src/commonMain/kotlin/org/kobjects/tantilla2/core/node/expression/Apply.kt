package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.node.Node


class Apply(
    val callable: Node,
    val parameters: List<Node>,
    val parameterSerialization: List<ParameterSerialization>,
    val implicit: Boolean,
    val asMethod: Boolean,
) : Node() {
    override fun eval(context: LocalRuntimeContext): Any? {
        context.checkState(this)
        val shouldBeLambda = callable.eval(context)
        if (shouldBeLambda !is Callable) {
            throw IllegalStateException("Lambda expected; got $shouldBeLambda")
        }
        val function = shouldBeLambda as Callable
        val functionContext = LocalRuntimeContext(
            context.globalRuntimeContext,
            function.scopeSize, {
            if (it < parameters.size) {
                val result = parameters[it].eval(context)
                // println("Result $result")
                result
            } else null
        }, function.closure)
        return function.eval(functionContext)
    }

    override fun children(): List<Node> = List(parameters.size + 1) {
       if (it == 0) callable else parameters[it - 1]
    }

    override fun reconstruct(newChildren: List<Node>): Node =
        Apply(newChildren[0], newChildren.subList(1, newChildren.size), parameterSerialization, implicit, asMethod)

    override val returnType
        get() = (callable.returnType as FunctionType).returnType

    override fun serializeCode(sb: CodeWriter, parentPrcedence: Int) {
        if (asMethod) {
            sb.appendCode(parameters[0])
            sb.append(".")
        }
        sb.appendCode(callable)
        if (!implicit) {
            sb.append("(")
            for (i in parameterSerialization.indices) {
                val parameter = parameterSerialization[i]
                if (i > 0) {
                    sb.append(", ")
                }
                if (parameter.named.isNotEmpty()) {
                    sb.append(parameter.named)
                    sb.append(" = ")
                }
                sb.appendCode(parameter.node)
            }
            sb.append(")")
        }
    }

    class ParameterSerialization(
        val named: String,
        val node: Node,
    )

}
