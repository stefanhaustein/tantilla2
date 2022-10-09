package org.kobjects.tantilla2.core.node

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.Type
import org.kobjects.tantilla2.core.builtin.RootScope
import org.kobjects.tantilla2.core.function.FunctionType
import org.kobjects.tantilla2.core.function.Callable
import org.kobjects.tantilla2.core.function.CallableImpl

class LambdaReference(
    val type: FunctionType,
    val scopeSize: Int,
    val body: Evaluable<LocalRuntimeContext>
) : TantillaNode {
    override val returnType: Type
        get() = type

    override fun children(): List<Evaluable<LocalRuntimeContext>> = emptyList()

    override fun eval(context: LocalRuntimeContext): Callable {
        return CallableImpl(type, scopeSize, body, context)
    }

    override fun reconstruct(newChildren: List<Evaluable<LocalRuntimeContext>>) = this

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.append("lambda ")
        writer.appendType(type, null)
        writer.append(":")
        writer.indent()
        writer.newline()
        writer.appendCode(body)
        writer.outdent()
    }
}