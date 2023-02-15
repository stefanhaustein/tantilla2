package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.function.LocalVariableDefinition
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.node.Node

class LocalVariableReference(
    val variable: LocalVariableDefinition,
    val depth: Int,
) : Node() {
    override fun children(): List<Node> = emptyList()

    override val returnType: Type
        get() = variable.type

    override fun eval(ctx: LocalRuntimeContext): Any {
        //        println("Evaluating $name")

        var varCtx = ctx
        for (i in 0 until depth) {
            varCtx = varCtx.scope.closure!!
        }

        val result = variable.getValue(varCtx)
        // println("Eval result for $name = $result")
        return result
    }

    override fun reconstruct(newChildren: List<Node>): Node =
        this

    override fun assign(context: LocalRuntimeContext, value: Any) {
        var varCtx = context
        for (i in 0 until depth) {
            varCtx = varCtx.scope.closure!!
        }
        variable.setValue(varCtx, value) /*
        if (index >= varCtx.variables.size) {
            throw IllegalStateException("Variable $name index $index not found!")
        }
        varCtx.variables[index] = value */
    }

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.append(variable.name)
    }

    override fun requireAssignability(): Node {
        if (!variable.mutable) {
            throw IllegalStateException("Local variable '${variable.name}' is not mutable.")
        }
        return this
    }
}