package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.CallableImpl
import org.kobjects.tantilla2.core.node.Node

class LambdaReference(
    val type: FunctionType,
    val scopeSize: Int,
    val body: Node,
    val implicit: Boolean
) : Node() {
    override val returnType: Type
        get() = type

    override fun children(): List<Node> =  listOf(body)

    override fun eval(context: LocalRuntimeContext): Callable {
        return CallableImpl(type, scopeSize, body, context)
    }

    override fun reconstruct(newChildren: List<Node>) = LambdaReference(type, scopeSize, newChildren[0], implicit)

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        if (implicit) {
            writer.appendCode(body)
        } else {
            writer.append("lambda ")
            writer.appendType(type, null)
            writer.append(":")
            writer.indent()
            writer.newline()
            writer.appendCode(body)
            writer.outdent()
        }
    }
}