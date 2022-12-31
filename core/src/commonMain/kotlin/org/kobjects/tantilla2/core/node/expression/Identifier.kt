package org.kobjects.tantilla2.core.node.expression

import org.kobjects.tantilla2.core.CodeWriter
import org.kobjects.tantilla2.core.LocalRuntimeContext
import org.kobjects.tantilla2.core.node.LeafNode
import org.kobjects.tantilla2.core.type.Type
import org.kobjects.tantilla2.core.type.VoidType

class Identifier(val name: String) : LeafNode() {

    override fun eval(context: LocalRuntimeContext) = throw UnsupportedOperationException()

    override val returnType: Type
        get() = VoidType

    override fun serializeCode(writer: CodeWriter, parentPrecedence: Int) {
        writer.append(name)
    }
}