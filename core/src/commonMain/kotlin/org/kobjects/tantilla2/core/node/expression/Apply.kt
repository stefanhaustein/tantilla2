package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.Precedence
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.FunctionDefinition
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.NoneType
import org.kobjects.tantilla2.core.type.Type


class Apply(
    val base: Node,
    override val returnType: Type,
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
            } else NoneType.None
        }
        return callable.eval(functionContext)
    }

    override fun children(): List<Node> = List(parameters.size + 1) {
       if (it == 0) base else parameters[it - 1]
    }

    override fun reconstruct(newChildren: List<Node>): Node =
        Apply(newChildren[0], returnType, newChildren.subList(1, newChildren.size), parameterSerialization, parens, asMethod)

    override fun serializeCode(writer: CodeWriter, parentPrcedence: Int) {
        if (asMethod) {
            writer.appendCode(parameters[0], Precedence.DOT)
            val mark = writer.mark()
            writer.append(".")
            writer.appendCode(base)
            writer.unmark(mark)
            if (writer.x + 1 >= writer.lineLength) {
                writer.reset(mark)
                writer.newline()
                writer.append(".")
                writer.appendCode(base)
            }
        } else {
            writer.appendCode(base)
        }

        for (parameter in parameterSerialization.filter { it.format == ParameterSerialization.Format.IN }) {
            writer.append(" ")
            writer.appendInfix(parentPrcedence, RawIdentifier(parameter.named), "in", Precedence.FOR_IN, parameter.node)
        }

        val nodeList = mutableListOf<Node>()
        val prefixList = mutableListOf<String>()

        for (parameter in parameterSerialization.filter { it.format == ParameterSerialization.Format.REGULAR }) {
            nodeList.add(parameter.node)
            if (parameter.named.isNotEmpty()) {
                prefixList.add(parameter.named + " = ")
            } else {
                prefixList.add("")
            }
        }
        if (parens) {
            writer.appendInBrackets("(", ")") {
                writer.appendList(nodeList, prefixList)
            }
        } else if (nodeList.isNotEmpty()) {
            writer.append(" ")
            writer.appendList(nodeList, prefixList)
        }
        for (parameter in parameterSerialization.filter { it.format == ParameterSerialization.Format.TRAILING_CLOSURE }) {
            if (parameter.named.isNotEmpty()) {
                writer.newline()
                writer.append(parameter.named)
            }

            if (parameter.node is PairNode) {
                writer.append(' ')
                writer.appendCode(parameter.node.a)
                writer.append(":")
                writer.indent()
                writer.newline()
                writer.appendCode(parameter.node.b)
                writer.outdent()
            } else {
                writer.append(":")
                writer.indent()
                writer.newline()
                writer.appendCode(parameter.node)
                writer.outdent()
            }
        }
    }

    data class ParameterSerialization(
        val named: String,
        val node: Node,
        val format: Format = Format.REGULAR
    ) {
        enum class Format {
            REGULAR, TRAILING_CLOSURE, IN
        }

    }

    override fun requireAssignability(): Node {
        require(asMethod) { "Method required for assignment." }
        require(parameters.size == 1)  { "No parameters permitted for assignment." }

        if (base !is StaticReference) {
            throw IllegalArgumentException("Method required for assignment; got $base")
        }
        val setMethodName = "set_" + base.definition.name
        val setMethod = parameters[0].returnType.resolve(setMethodName)
        if (setMethod !is FunctionDefinition) {
            throw IllegalArgumentException("Can't resolve '$setMethodName'")
        }

        return SetMethodCall(setMethod, parameters[0])
    }

}
