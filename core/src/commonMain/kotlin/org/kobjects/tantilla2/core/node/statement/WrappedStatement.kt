package org.kobjects.tantilla2.core.node.statement

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.control.LoopControlSignal
import org.kobjects.tantilla2.core.control.TantillaControlSignal
import org.kobjects.tantilla2.core.control.TantillaRuntimeException
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.NoneType
import org.kobjects.tantilla2.core.node.Node

class WrappedStatement(
    val statement: Node
): Node() {

    override val returnType: Type
        get() = statement.returnType


    override fun eval(env: LocalRuntimeContext): Any {

            try {
                return statement.eval(env)
            } catch (e: TantillaControlSignal) {
                throw e
            } catch (e: TantillaRuntimeException) {
                throw e
            } catch (e: Exception) {
                throw env.globalRuntimeContext.createException(null, this, null, e)
            }

    }

    override fun children() = listOf(statement)

    override fun reconstruct(newChildren: List<Node>) =
        WrappedStatement(statement = newChildren.first())

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.appendCode(statement)
        }
}