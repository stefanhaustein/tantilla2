package org.kobjects.tantilla2.core.node.statement

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.control.LoopControlSignal
import org.kobjects.tantilla2.core.control.TantillaControlSignal
import org.kobjects.tantilla2.core.control.TantillaRuntimeException
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.NoneType
import org.kobjects.tantilla2.core.node.Node

class BlockNode(
    vararg val statements: Node
): Node() {

    override val returnType: Type
        get() = if (statements.isEmpty()) NoneType else statements.last().returnType


    override fun eval(env: LocalRuntimeContext): Any {
        var result: Any = NoneType.None
        for (statement: Node in statements) {
            try {
                result = statement.eval(env)
            } catch (e: TantillaControlSignal) {
                throw e
            } catch (e: TantillaRuntimeException) {
                throw e
            } catch (e: Exception) {
                throw env.globalRuntimeContext.createException(null, this, null, e)
            }
        }
        return result
    }

    override fun children() = statements.asList()

    override fun reconstruct(newChildren: List<Node>) =
        BlockNode(statements = newChildren.toTypedArray())

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        if (statements.isNotEmpty()) {
            writer.appendCode(statements[0])
            for (i in 1 until statements.size) {
               writer.newline()
               writer.appendCode(statements[i])
            }
        }
    }
}