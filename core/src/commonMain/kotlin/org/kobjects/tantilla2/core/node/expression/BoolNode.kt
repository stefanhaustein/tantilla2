package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.Precedence
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.BoolType
import org.kobjects.tantilla2.core.type.Type

object BoolNode {

    class And(private val left: Node, private val right: Node) : Node() {
        override fun eval(context: LocalRuntimeContext): Boolean =
            (left.eval(context) as Boolean) && (right.eval(context) as Boolean)

        override val returnType: Type
            get() = BoolType

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.appendInfix(this, parentPrecedence, "and", Precedence.LOGICAL_AND)
        }

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Node>) = And(newChildren[0], newChildren[1])
    }

    class Or(private val left: Node, private val right: Node) : Node() {
        override fun eval(context: LocalRuntimeContext): Boolean =
            (left.eval(context) as Boolean) && (right.eval(context) as Boolean)

        override val returnType: Type
            get() = BoolType

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.appendInfix(this, parentPrecedence, "and", Precedence.LOGICAL_AND)
        }

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Node>) = Or(newChildren[0], newChildren[1])
    }

    class Not(private val operand: Node) : Node() {
        override fun eval(context: LocalRuntimeContext): Boolean = !(operand.eval(context) as Boolean)

        override val returnType: Type
            get() = BoolType

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.appendInfix(this, parentPrecedence, "and", Precedence.LOGICAL_AND)
        }

        override fun reconstruct(newChildren: List<Node>) = Not(newChildren[0])

        override fun children() = listOf(operand)

    }


}