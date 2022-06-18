package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Lambda
import org.kobjects.tantilla2.core.function.LambdaImpl

class LambdaReference(
    val type: FunctionType,
    val scopeSize: Int,
    val body: Evaluable<RuntimeContext>,
    val closureIndices: List<Int>,
) : TantillaNode {
    override val returnType: Type
        get() = type

    override fun children(): List<Evaluable<RuntimeContext>> = emptyList()

    override fun eval(context: RuntimeContext): Lambda {
        val closure = List<Any?>(closureIndices.size) { context[closureIndices[it]] }
        return LambdaImpl(type, scopeSize, body, closure)
    }

    override fun reconstruct(newChildren: List<Evaluable<RuntimeContext>>) = this

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.append("lambda ")
        writer.appendType(type)
        writer.append(":")
        writer.indent()
        writer.newline()
        writer.appendCode(body)
        writer.outdent()
    }
}