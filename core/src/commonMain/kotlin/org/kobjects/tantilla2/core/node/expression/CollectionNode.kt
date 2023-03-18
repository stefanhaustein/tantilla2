package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.Precedence
import org.kobjects.tantilla2.core.collection.CollectionType
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.BoolType
import org.kobjects.tantilla2.core.type.Type

object CollectionNode {

    class In(
        val valueExpr: Node,
        val collectionExpr: Node,
    ) : Node() {
        override fun children() = listOf(valueExpr, collectionExpr)

        override fun reconstruct(newChildren: List<Node>) = In(newChildren[0], newChildren[1])

        override fun eval(context: LocalRuntimeContext): Boolean {
           val haystack = collectionExpr.eval(context)
            val needle = valueExpr.eval(context)
            if (haystack is Map<*,*>) {
               return (haystack as Map<Any, Any>).containsKey(valueExpr.eval(context))
            }
            return (haystack as Collection<Any>).contains(needle)
        }
        override val returnType: Type
            get() = BoolType

        override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
            writer.appendInfix(parentPrecedence, valueExpr,"in", Precedence.RELATIONAL, collectionExpr)
        }

        init {
            val returnType = collectionExpr.returnType
            if (returnType.unparameterized() !is CollectionType) {
                throw IllegalArgumentException("Collection expected for 'in'")
            }
            if (!returnType.genericParameterTypes[0].isAssignableFrom(valueExpr.returnType)) {
                throw IllegalArgumentException("Type ${valueExpr.returnType} can't be contained in a collection of type $returnType")
            }
        }

    }

}