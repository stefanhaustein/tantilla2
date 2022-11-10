package org.kobjects.tantilla2.core.node

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.SerializableCode
import org.kobjects.tantilla2.core.Type

class LocalVariableReference(
    val name: String,
    override val returnType: Type,
    val depth: Int,
    val index: Int,
    val mutable: Boolean,
) : Assignable, SerializableCode {
    override fun children(): List<Evaluable> = emptyList()

    override fun eval(ctx: LocalRuntimeContext): Any? {
        //        println("Evaluating $name")

        var varCtx = ctx
        for (i in 0 until depth) {
            varCtx = varCtx.closure!!
        }

        val result = varCtx[index]
        // println("Eval result for $name = $result")
        return result
    }

    override fun reconstruct(newChildren: List<Evaluable>): Evaluable =
        this

    override fun assign(context: LocalRuntimeContext, value: Any?) {
        if (index >= context.variables.size) {
            throw IllegalStateException("Variable $name index $index not found!")
        }
        context.variables[index] = value
    }

    override fun serializeCode(writer: CodeWriter, precedence: Int) {
        writer.append(name)
    }

    override fun toString() = name
}