package org.kobjects.tantilla2.core.function

import org.kobjects.tantilla2.core.Evaluable
import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.SerializableCode
import org.kobjects.tantilla2.core.control.FlowSignal

class CallableImpl(
    override val type: FunctionType,
    override val dynamicScopeSize: Int,
    val body: Evaluable,
    override val closure: LocalRuntimeContext? = null
    ) : Callable, SerializableCode {


    // get() = "(${type.parameters}) -> ${type.returnType}"

    override fun eval(context: LocalRuntimeContext): Any {

        if (closure != context.scope.closure) {
            throw RuntimeException("closure mismatch")
        }

        val result = body.eval(context)
        if (result is FlowSignal && closure == null) {
            if (result.kind == FlowSignal.Kind.RETURN) {
                return result.value
            }
            throw IllegalStateException("Unexpected signal: $result")
        }
        return result
    }

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.appendType(type)
        writer.append(":").indent().newline()
        writer.appendCode(body)
        writer.outdent()
    }

    override fun toString() = CodeWriter().appendCode(this).toString()
}