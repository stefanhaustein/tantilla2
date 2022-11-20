package org.kobjects.tantilla2.core.node.statement

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.commonType
import org.kobjects.tantilla2.core.node.Node

class IfNode(
    vararg val ifThenElse: Node,
) : Node() {
    override val returnType: Type
        get() = commonType(ifThenElse.filterIndexed { index, node -> index and 1 == 1 }.map { it.returnType })

    override fun eval(env: LocalRuntimeContext): Any? {
        for (i in ifThenElse.indices step 2) {
            if (i == ifThenElse.size - 1) {
                return ifThenElse[i].eval(env)
            } else if (ifThenElse[i].eval(env) as Boolean) {
                return ifThenElse[i + 1].eval(env)
            }
        }
        return Unit
    }

    override fun children() = ifThenElse.toList()

    override fun reconstruct(newChildren: List<Node>) = IfNode(*newChildren.toTypedArray())

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.append("if ")
        writer.appendCode(ifThenElse[0])
        writer.append(':').indent().newline()
        writer.appendCode(ifThenElse[1])
        writer.outdent()

        for (i in 2 until ifThenElse.size - 1 step 2) {
            writer.newline().append("elif ").indent()
            writer.appendCode(ifThenElse[i])
            writer.append(':').newline()
            writer.appendCode(ifThenElse[i + 1])
            writer.outdent()
        }
        if (ifThenElse.size % 2 == 1) {
            writer.newline().append("else:").indent().newline()
            writer.appendCode(ifThenElse[ifThenElse.size - 1])
            writer.outdent()
        }
    }

}