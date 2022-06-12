package org.kobjects.tantilla2.core.function

import org.kobjects.greenspun.core.Control
import org.kobjects.greenspun.core.Evaluable
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.RuntimeContext
import org.kobjects.tantilla2.core.SerializableCode
import org.kobjects.tantilla2.core.serializeCode

class LambdaImpl(
    override val type: FunctionType,
    override val scopeSize: Int,
    val body: Evaluable<RuntimeContext>,
    ) : Lambda, SerializableCode {


    // get() = "(${type.parameters}) -> ${type.returnType}"

    override fun eval(context: RuntimeContext): Any? {
        val result = body.eval(context)
        if (result is Control.FlowSignal) {
            if (result.kind == Control.FlowSignal.Kind.RETURN) {
                return result.value
            }
            throw IllegalStateException("Unexpected signal: $result")
        }
        return result
    }

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.appendType(type)
        writer.append(":").indent().newline()
        writer.appendCode(body)
        writer.outdent()
    }

    override fun toString() = CodeWriter().appendCode(this).toString()
}