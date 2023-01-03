package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.node.LeafNode
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.UnresolvedType

object UnresolvedNode : LeafNode() {
    override fun eval(context: LocalRuntimeContext): Any {
        throw UnsupportedOperationException("Unresolved node")
    }

    override val returnType: Type
        get() = UnresolvedType

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.append("<Unresolved Node>")
    }
}