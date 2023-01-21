package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Evaluable
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.function.FunctionDefinition
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.Type

class SetMethodCall(
    val definition: FunctionDefinition,
    val base: Node,
) : Node() {
    override fun children() = listOf(base)

    override fun reconstruct(newChildren: List<Node>) = SetMethodCall(definition, newChildren[0])

    override fun eval(context: LocalRuntimeContext): Any {
        throw UnsupportedOperationException()
    }

    override val returnType: Type
        get()= definition.returnType

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.appendCode(base)
        writer.append(".")
        writer.append(definition.name.substring(4))
    }


    override fun assign(context: LocalRuntimeContext, value: Any) {
        val self = base.eval(context)
        val context = LocalRuntimeContext(context.globalRuntimeContext, definition) {
            when (it) {
                0 -> self
                1 -> value
                else -> throw IllegalArgumentException()
            }
        }
        definition.eval(context)
    }
}