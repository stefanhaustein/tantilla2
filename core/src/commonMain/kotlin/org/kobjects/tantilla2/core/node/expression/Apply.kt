package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.Precedence
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.VoidType


class Apply(
    val base: Node,
    val parameters: List<Node>,
    val parameterSerialization: List<ParameterSerialization>,
    val parens: Boolean,
    val asMethod: Boolean,
) : Node() {
    override fun eval(context: LocalRuntimeContext): Any {
        context.checkState(this)
        val callable = base.eval(context)
        if (callable !is Callable) {
            throw IllegalStateException("Callable expected; got $callable")
        }
        val functionContext = LocalRuntimeContext(
            context.globalRuntimeContext,
            callable
        ) {
            if (it < parameters.size) {
                val result = parameters[it].eval(context)
                // println("Result $result")
                result
            } else VoidType.None
        }
        return callable.eval(functionContext)
    }

    override fun children(): List<Node> = List(parameters.size + 1) {
       if (it == 0) base else parameters[it - 1]
    }

    override fun reconstruct(newChildren: List<Node>): Node =
        Apply(newChildren[0], newChildren.subList(1, newChildren.size), parameterSerialization, parens, asMethod)

    override val returnType
        get() = (base.returnType as FunctionType).returnType

    override fun serializeCode(writer: CodeWriter, parentPrcedence: Int) {
        if (asMethod) {
            writer.appendCode(parameters[0], Precedence.DOT)
            writer.append(".")
        }
        writer.appendCode(base)

        val nodeList = mutableListOf<Node>()
        val prefixList = mutableListOf<String>()
        if (parens) {
            writer.appendOpen('(')
        } else {
            writer.append(' ')
        }
        for (i in parameterSerialization.indices) {
            val parameter = parameterSerialization[i]
            nodeList.add(parameter.node)
            if (parameter.named.isNotEmpty()) {
                prefixList.add(parameter.named + " = ")
            } else {
                prefixList.add("")
            }
        }
        writer.appendList(nodeList, prefixList)
        if (parens) {
            writer.appendClose(')')
        }
    }

    class ParameterSerialization(
        val named: String,
        val node: Node,
    )

}
