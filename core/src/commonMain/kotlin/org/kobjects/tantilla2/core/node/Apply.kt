package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.Parameter
import org.kobjects.tantilla2.core.returnType


class Apply(
    val callable: Evaluable<LocalRuntimeContext>,
    val parameterDeclarations: List<Parameter>,
    val parameters: List<Evaluable<LocalRuntimeContext>>,
    val implicit: Boolean,
    val asMethod: Boolean,
) : TantillaNode {
    override fun eval(context: LocalRuntimeContext): Any? {
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

    override fun children(): List<Evaluable<LocalRuntimeContext>> = List(parameters.size + 1) {
       if (it == 0) callable else parameters[it - 1]
    }

    override fun reconstruct(newChildren: List<Evaluable<LocalRuntimeContext>>): Evaluable<LocalRuntimeContext> =
        Apply(newChildren[0], parameterDeclarations, newChildren.subList(1, newChildren.size), implicit, asMethod)

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
            var first = true
            for (i in startIndex until parameters.size) {
                if (parameterDeclarations[i].isVararg) {
                    for (sub in (parameters[i] as ListLiteral).elements) {
                        if (first) {
                            first = false
                        } else {
                            sb.append(", ")
                        }
                        sb.appendCode(sub)
                    }
                } else {
                    if (first) {
                        first = false
                    } else {
                        sb.append(", ")
                    }
                    sb.appendCode(parameters[i])
                }
            }
            sb.append(")")
        }
    }

    override fun toString(): String = CodeWriter().appendCode(this).toString()
}
