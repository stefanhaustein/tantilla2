package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.Evaluable
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.definition.ContextOwner
import org.kobjects.tantilla2.core.node.Node
import org.kobjects.tantilla2.core.type.Type

class FakeLambda(var node: Node) : Node() {
    override fun children() = listOf(node)

    override fun reconstruct(newChildren: List<Node>) = FakeLambda(newChildren[0])

    override fun eval(context: LocalRuntimeContext) = node.eval(LocalRuntimeContext(context.globalRuntimeContext, object : ContextOwner {
        override val dynamicScopeSize: Int
            get() = 0
        override val closure: LocalRuntimeContext
            get() = context
    }))


    override val returnType: Type
        get() = node.returnType

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) = node.serializeCode(writer, parentPrecedence)

}