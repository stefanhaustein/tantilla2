package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.node.Node

class LocalVariableReference(
    val name: String,
    override val returnType: Type,
    val depth: Int,
    val index: Int,
    val mutable: Boolean,
) : Node() {
    override fun children(): List<Node> = emptyList()

    override fun eval(ctx: LocalRuntimeContext): Any {
        //        println("Evaluating $name")

        var varCtx = ctx
        for (i in 0 until depth) {
            varCtx = varCtx.scope.closure!!
        }

        val result = varCtx[index]
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
        if (index >= varCtx.variables.size) {
            throw IllegalStateException("Variable $name index $index not found!")
        }
        varCtx.variables[index] = value
    }

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.append(name)
    }

    override fun requireAssignability(): Node {
        if (!mutable) {
            throw IllegalStateException("Local variable '$name' is not mutable.")
        }
        return this
    }
}